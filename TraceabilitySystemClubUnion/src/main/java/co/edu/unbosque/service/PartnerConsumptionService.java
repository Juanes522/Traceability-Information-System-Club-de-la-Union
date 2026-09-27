package co.edu.unbosque.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.dto.ConsumptionCreateRequest;
import co.edu.unbosque.dto.NotificationDTO;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditResult;
import co.edu.unbosque.model.Notification;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.NotificationRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;
import co.edu.unbosque.repository.PersonPartnerRepository;
import co.edu.unbosque.security.HttpRequestUtils;

/**
 * Registro de consumos y consulta de los avisos que generan.
 *
 * <p>Es el servicio con más colaboradores del sistema —siete— y el que concentra la operación central: registrar
 * un cargo contra la acción de un socio y notificárselo. Esa concentración es deliberada: el registro de un
 * consumo es una unidad de negocio que debe persistir el hecho contable y avisar al interesado en el mismo acto.
 *
 * <p>La clase es {@code @Transactional} completa, de modo que toda operación de escritura y sus efectos
 * secundarios comparten transacción, con una excepción intencionada: el registro de presencia se ejecuta en una
 * transacción independiente.
 *
 * <p>Las reglas de negocio están documentadas en
 * {@link #register(co.edu.unbosque.dto.ConsumptionCreateRequest)}, que es donde viven.
 *
 * @see co.edu.unbosque.model.PartnerConsumption
 * @see AccessService
 */
@Service
@Transactional
public class PartnerConsumptionService {

    private final PartnerConsumptionRepository consumptionRepo;
    private final PersonPartnerRepository partnerRepo;
    private final NotificationRepository notRepo;
    private final PushNotificationService pushService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final AccessService accessService;

    public PartnerConsumptionService(PartnerConsumptionRepository consumptionRepo,
                                     PersonPartnerRepository partnerRepo,
                                     NotificationRepository notRepo,
                                     PushNotificationService pushService,
                                     EmailService emailService,
                                     AuditService auditService,
                                     AccessService accessService) {
        this.consumptionRepo = consumptionRepo;
        this.partnerRepo     = partnerRepo;
        this.notRepo         = notRepo;
        this.pushService     = pushService;
        this.emailService    = emailService;
        this.auditService    = auditService;
        this.accessService   = accessService;
    }

    /**
     * Registra un consumo y desencadena la notificación al socio.
     *
     * <p>Es <strong>el flujo central del sistema</strong>: la operación por la que existe la trazabilidad. Todo
     * ocurre dentro de la transacción que declara la clase.
     *
     * <p>Secuencia: resuelve el socio, construye el consumo, calcula y adjunta las líneas de detalle, guarda
     * —las líneas se persisten en cascada—, registra la presencia, calcula el total, persiste la notificación y
     * dispara las tres entregas (Web Push, correo y auditoría).
     *
     * <h4>Reglas de negocio que viven exclusivamente aquí</h4>
     *
     * <ul>
     *   <li><strong>Cierre sintético a los 20 minutos.</strong> Si el cliente no envía
     *       {@code consumptionClosing}, se fija en la apertura más veinte minutos. No hay ninguna otra
     *       definición de la duración de un consumo en el sistema, y ese valor acabará en las métricas como si
     *       fuera un dato medido.</li>
     *   <li><strong>Importe de línea.</strong> Cada {@code lineTotal} se calcula como
     *       {@code unitPrice * quantity} y se almacena denormalizado. Nada lo recalcula después, y nada
     *       comprueba que la suma de las líneas coincida con el {@code consumptionValue} declarado.</li>
     *   <li><strong>Total del cargo</strong> = {@code consumptionValue + iva + service + tip}, con nulos como
     *       cero. Es una de las siete implementaciones independientes de esa fórmula que hay en el sistema.</li>
     *   <li><strong>La presencia se infiere del consumo</strong>, usando la hora de apertura y no la actual.</li>
     * </ul>
     *
     * <h4>Tolerancia a fallos: el cargo prevalece sobre el aviso</h4>
     *
     * <p>El registro de presencia va en {@code try/catch}, y las tres entregas capturan sus excepciones
     * internamente. La decisión de diseño es clara y razonable —<strong>el dato contable es lo importante; el
     * aviso es accesorio</strong>—, pero tiene una consecuencia que conviene conocer: un socio puede quedar sin
     * enterarse de un cargo sin que nada lo señale más allá de una línea en {@code System.err}.
     *
     * <h4>Consideraciones técnicas</h4>
     *
     * <ul>
     *   <li><strong>No hay control de autorización ni de propiedad.</strong> El {@code partnerId} se toma del
     *       cuerpo de la petición y el endpoint que invoca este método carece de {@code @PreAuthorize}.</li>
     *   <li>El envío Web Push es <strong>sincrónico y dentro de la transacción</strong>: mantiene abiertos los
     *       bloqueos y la conexión mientras se completan las peticiones HTTPS a los servicios push.</li>
     *   <li>La notificación se persiste sin usar
     *       {@link co.edu.unbosque.model.PartnerConsumption#addNotification}, de modo que la colección en
     *       memoria del consumo queda desactualizada dentro de la misma transacción.</li>
     *   <li>El evento de auditoría se <strong>retrodata</strong> a la hora de apertura del consumo, no al
     *       instante del registro.</li>
     *   <li>La comprobación inicial de {@code partnerId} nulo es inalcanzable por HTTP, porque la validación
     *       {@code @NotNull} del DTO actúa antes.</li>
     * </ul>
     *
     * @param req datos del consumo, ya validados por Bean Validation
     * @return el consumo persistido. Al serializarse no incluye sus líneas ni sus notificaciones, pero sí el
     *         número de acción derivado
     * @throws RuntimeException si el socio no existe. El controlador captura toda excepción y responde 400, y es
     *                          por eso que «socio no encontrado» se manifiesta como 400 y no como 404
     */
    public PartnerConsumption register(ConsumptionCreateRequest req) {
        if (req.getPartnerId() == null)
            throw new RuntimeException("Partner id is null");

        PersonPartner partner = partnerRepo.findByPersonId(req.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        PartnerConsumption consumption = new PartnerConsumption();
        consumption.setPartner(partner);
        consumption.setEnviroment(req.getEnviroment());
        consumption.setAccount(req.getAccount());
        consumption.setTable(req.getTable());
        consumption.setWaiterName(req.getWaiterName());
        consumption.setIsPartner(req.getIsPartner());
        consumption.setConsumptionValue(req.getConsumptionValue());
        consumption.setIva(req.getIva());
        consumption.setService(req.getService());
        consumption.setTip(req.getTip());
        consumption.setConsumptionOpening(
                req.getConsumptionOpening() != null
                        ? req.getConsumptionOpening()
                        : LocalDateTime.now());
        LocalDateTime opening = consumption.getConsumptionOpening();
        consumption.setConsumptionClosing(
                req.getConsumptionClosing() != null
                        ? req.getConsumptionClosing()
                        : opening.plusMinutes(20));

        if (req.getItems() != null) {
            for (co.edu.unbosque.dto.ConsumptionItemRequest ir : req.getItems()) {
                co.edu.unbosque.model.ConsumptionItem item = new co.edu.unbosque.model.ConsumptionItem();
                item.setProductId(ir.getProductId());
                item.setName(ir.getName());
                item.setQuantity(ir.getQuantity());
                item.setUnitPrice(ir.getUnitPrice());
                item.setLineTotal(ir.getUnitPrice() * ir.getQuantity());
                item.setCategory(ir.getCategory());
                item.setSubcategory(ir.getSubcategory());
                item.setDishType(ir.getDishType());
                consumption.addItem(item);
            }
        }

        PartnerConsumption saved = consumptionRepo.save(consumption);

        try {
            accessService.registerPresence(partner, opening);
        } catch (Exception e) {
            System.err.println("Presence registration failed: " + e.getMessage());
        }

        double total = safe(req.getConsumptionValue()) + safe(req.getIva())
                     + safe(req.getService()) + safe(req.getTip());

        String title = "Nuevo cargo registrado";
        String body  = String.format("Se registró un cargo de $%.2f en %s · Mesa %s · Mesero: %s",
                total, req.getEnviroment(), req.getTable(), req.getWaiterName());

        Notification notification = new Notification();
        notification.setConsumption(saved);
        notification.setNotificationType("CHARGE_NOTIFICATION");
        notification.setTitle(title);
        notification.setBody(body);
        notification.setGenerationDate(opening);
        notification.setState('S');
        notRepo.save(notification);

        pushService.sendToPartner(partner.getIdentification(), title, body);
        emailService.sendConsumptionNotificationEmail(partner, saved, total);

        auditService.record(AuditEventType.CHARGE_REGISTERED, AuditResult.SUCCESS,
                partner.getIdentification(), HttpRequestUtils.currentClientIp(),
                String.format("Cargo de $%.2f en %s", total, req.getEnviroment()),
                String.valueOf(saved.getConsumptionId()),
                opening.atZone(java.time.ZoneId.systemDefault()).toInstant());

        return saved;
    }

    /**
     * Devuelve los consumos de un ambiente sin paginar. Sin uso actualmente.
     *
     * @param enviroment nombre exacto del ambiente
     * @return los consumos, o {@code null} si no hay ninguno
     */
    public List<PartnerConsumption> getByEnviroment(String enviroment) {
        List<PartnerConsumption> list = consumptionRepo.findByEnviroment(enviroment);
        return list.isEmpty() ? null : list;
    }

    /**
     * Devuelve una página de consumos de un ambiente, opcionalmente acotada por fechas.
     *
     * <p>El filtro temporal se aplica <strong>solo si ambos extremos son no nulos</strong>; con uno solo, la
     * ventana se ignora por completo y se devuelve el histórico entero paginado. El controlador acota el rango a
     * 92 días, pero no exige que se envíe.
     *
     * @param env      nombre exacto del ambiente
     * @param from     inicio del rango, o {@code null}
     * @param to       fin del rango, o {@code null}
     * @param pageable página solicitada
     * @return la página de consumos
     */
    public Page<PartnerConsumption> getByEnviromentPaged(String env, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null) {
            return consumptionRepo.findByEnviromentAndConsumptionOpeningBetween(env, from, to, pageable);
        }
        return consumptionRepo.findByEnviroment(env, pageable);
    }

    /**
     * Devuelve una página de consumos de un socio, opcionalmente acotada por fechas.
     *
     * <p>Sirve tanto {@code /personpartner/getconsumptions/me} como su variante por identificación para gestores,
     * con la misma semántica de ventana opcional descrita en
     * {@link #getByEnviromentPaged(String, LocalDateTime, LocalDateTime, Pageable)}.
     *
     * @param personId clave primaria del socio
     * @param from     inicio del rango, o {@code null}
     * @param to       fin del rango, o {@code null}
     * @param pageable página solicitada, normalmente ordenada por apertura descendente
     * @return la página de consumos
     */
    public Page<PartnerConsumption> getByPartnerPaged(Long personId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null) {
            return consumptionRepo.findByPartnerPersonIdAndConsumptionOpeningBetween(personId, from, to, pageable);
        }
        return consumptionRepo.findByPartnerPersonId(personId, pageable);
    }

    /**
     * Devuelve todos los consumos de un socio, sin paginar.
     *
     * <p>Lo consume {@code GET /partnerconsumption/by-partner/{partnerId}}, protegido por la única guarda
     * explícita contra IDOR del sistema. El frontend no lo usa.
     *
     * @param partnerPersonId clave primaria del socio
     * @return los consumos, o {@code null} si no hay ninguno
     */
    public List<PartnerConsumption> getByPartnerId(Long partnerPersonId) {
        List<PartnerConsumption> list = consumptionRepo.findByPartnerPersonId(partnerPersonId);
        return list.isEmpty() ? null : list;
    }

    /**
     * Devuelve todos los avisos de un socio, sin paginar. Sin uso actualmente.
     *
     * @param identification identificación del socio, en claro
     * @return los avisos como DTO, del más reciente al más antiguo
     */
    public List<NotificationDTO> getNotificationsForPartner(String identification) {
        List<Notification> notifications = notRepo
                .findByConsumptionPartnerIdentificationOrderByGenerationDateDesc(identification);

        return notifications.stream().map(n -> {
            PartnerConsumption c = n.getConsumption();
            double total = safe(c.getConsumptionValue()) + safe(c.getIva())
                         + safe(c.getService()) + safe(c.getTip());
            return new NotificationDTO(n.getNotificationId(), n.getTitle(), n.getBody(),
                    n.getGenerationDate(), n.getState(), c.getConsumptionId(),
                    c.getEnviroment(), total);
        }).toList();
    }

    /**
     * Devuelve una página de avisos del socio, con el importe del cargo recalculado.
     *
     * <p>Sirve {@code GET /personpartner/notifications/me}. El importe <strong>no se lee del aviso</strong> —la
     * entidad no lo guarda—, sino que se recalcula desde el consumo asociado con la fórmula habitual.
     *
     * <p>Consideración de rendimiento: desreferenciar {@code n.getConsumption()} carga un proxy perezoso
     * <strong>por cada fila de la página</strong>, de modo que una página de diez avisos provoca diez consultas
     * adicionales. Es el precio de recalcular el total en lugar de almacenarlo en el aviso.
     *
     * @param identification identificación del socio, en claro
     * @param pageable       página solicitada
     * @return la página de avisos como DTO, del más reciente al más antiguo
     */
    public Page<NotificationDTO> getNotificationsForPartnerPaged(String identification, Pageable pageable) {
        return notRepo.findByConsumptionPartnerIdentificationOrderByGenerationDateDesc(identification, pageable)
                .map(n -> {
                    PartnerConsumption c = n.getConsumption();
                    double total = safe(c.getConsumptionValue()) + safe(c.getIva())
                            + safe(c.getService()) + safe(c.getTip());
                    return new NotificationDTO(n.getNotificationId(), n.getTitle(), n.getBody(),
                            n.getGenerationDate(), n.getState(), c.getConsumptionId(), c.getEnviroment(), total);
                });
    }

    /**
     * Convierte un importe nulo en cero.
     *
     * <p>Necesario porque ninguna columna monetaria de {@link co.edu.unbosque.model.PartnerConsumption} es
     * obligatoria: sin esta protección, un solo campo nulo propagaría un {@code NullPointerException} al calcular
     * el total. Variantes de este mismo método existen en otros cuatro servicios.
     *
     * @param v importe, posiblemente nulo
     * @return el importe, o {@code 0.0} si era nulo
     */
    private double safe(Double v) {
        return v != null ? v : 0.0;
    }
}
