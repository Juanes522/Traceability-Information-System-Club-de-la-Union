package co.edu.unbosque.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.model.Access;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.AccessRepository;

/**
 * Gestiona el registro de presencia de los socios en el club.
 *
 * <p>Custodia dos reglas de negocio que no existen en ningún otro lugar del sistema: la invariante de «una
 * sola visita abierta por socio» y el cierre masivo diario de visitas.
 *
 * <p>Conviene precisar qué significa «presencia» aquí: <strong>no se mide, se infiere</strong>. No hay
 * torniquete ni lector; es {@link PartnerConsumptionService#register} quien invoca este servicio al registrar
 * un cargo. Un socio que entre y no consuma nada no queda registrado, y por tanto toda métrica de afluencia
 * derivada mide actividad de consumo.
 *
 * @see co.edu.unbosque.model.Access
 */
@Service
public class AccessService {

	private final AccessRepository accessRepo;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param accessRepo el valor de access repo
	 */
	public AccessService(AccessRepository accessRepo) {
		this.accessRepo = accessRepo;
	}

	/**
	 * Registra la presencia del socio con la hora actual.
	 *
	 * <p><strong>Sin uso actualmente</strong>, y con una particularidad técnica que conviene señalar: delega en
	 * la sobrecarga de dos argumentos mediante {@code this}, es decir con una autoinvocación que
	 * <strong>esquiva el proxy de Spring</strong>. El {@code REQUIRES_NEW} de la sobrecarga no tendría por
	 * tanto efecto alguno: el trabajo se ejecutaría en la transacción del llamante.
	 *
	 * @param partner socio cuya presencia se registra
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registerPresence(PersonPartner partner) {
		registerPresence(partner, LocalDateTime.now());
	}

	/**
	 * Abre una visita para el socio, salvo que ya tenga una abierta.
	 *
	 * <p>Implementa la invariante <strong>«como máximo una visita abierta por socio»</strong>: sin ella, cada
	 * cargo del día abriría una visita nueva y las métricas de afluencia contarían una visita por consumo en
	 * lugar de una por estancia.
	 *
	 * <p>Se ejecuta en una transacción <strong>independiente</strong> ({@code REQUIRES_NEW}), decisión
	 * deliberada: la presencia se confirma aunque la transacción del consumo acabe revirtiéndose, y un fallo al
	 * registrarla no arrastra al cargo. El llamante refuerza esa independencia capturando la excepción.
	 *
	 * <p><strong>La comprobación previa a la inserción no es segura frente a concurrencia</strong> y no hay
	 * índice único que la respalde. Si dos registros simultáneos llegasen a abrir dos visitas del mismo socio,
	 * {@link co.edu.unbosque.repository.AccessRepository#findOpenAccessByPartnerId(Long)} empezaría a lanzar
	 * excepción en cada llamada posterior y, al estar capturada por el llamante, el registro de presencia de ese
	 * socio se detendría de forma silenciosa y permanente.
	 *
	 * @param partner socio cuya presencia se registra
	 * @param admissionTime hora de entrada; normalmente la apertura del consumo. Si es {@code null} se usa la
	 *                      hora actual
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registerPresence(PersonPartner partner, LocalDateTime admissionTime) {
		if (accessRepo.findOpenAccessByPartnerId(partner.getPersonId()).isEmpty()) {
			Access access = new Access();
			access.setPartner(partner);
			access.setDateTimeAdmission(admissionTime != null ? admissionTime : LocalDateTime.now());
			accessRepo.save(access);
		}
	}

	/**
	 * Cierra todas las visitas abiertas del sistema, cada día a las 02:00.
	 *
	 * <p>Existe porque nada cierra las visitas de forma individual: sin este trabajo, toda visita quedaría
	 * abierta indefinidamente y el indicador de «socios presentes ahora» crecería sin límite.
	 *
	 * <p><strong>Estampa la hora actual en todas las visitas abiertas, sin discriminar cuándo se
	 * abrieron.</strong> Consecuencias sobre la calidad del dato: una visita abierta a las 23:00 registra tres
	 * horas de duración, y una abierta a las 02:00:30 permanece abierta otras veinticuatro. La duración
	 * almacenada es por tanto una aproximación con sesgo sistemático, no una medición, y cualquier métrica de
	 * permanencia construida sobre ella hereda ese sesgo.
	 *
	 * <p>La hora elegida —02:00— es coherente con el cierre nocturno de un club, de modo que en operación
	 * normal la aproximación es razonable; se degrada solo con visitas que quedaron abiertas por error.
	 */
	@Transactional
	@Scheduled(cron = "0 0 2 * * *")
	public void closeOpenAccesses() {
		accessRepo.closeOpenAccesses(LocalDateTime.now());
	}
}
