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
            accessService.registerPresence(partner);
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
        notification.setGenerationDate(LocalDateTime.now());
        notification.setState('S');
        notRepo.save(notification);

        pushService.sendToPartner(partner.getIdentification(), title, body);
        emailService.sendConsumptionNotificationEmail(partner, saved, total);

        auditService.record(AuditEventType.CHARGE_REGISTERED, AuditResult.SUCCESS,
                partner.getIdentification(), HttpRequestUtils.currentClientIp(),
                String.format("Cargo de $%.2f en %s", total, req.getEnviroment()),
                String.valueOf(saved.getConsumptionId()));

        return saved;
    }

    public List<PartnerConsumption> getByEnviroment(String enviroment) {
        List<PartnerConsumption> list = consumptionRepo.findByEnviroment(enviroment);
        return list.isEmpty() ? null : list;
    }

    public Page<PartnerConsumption> getByEnviromentPaged(String env, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null) {
            return consumptionRepo.findByEnviromentAndConsumptionOpeningBetween(env, from, to, pageable);
        }
        return consumptionRepo.findByEnviroment(env, pageable);
    }

    public Page<PartnerConsumption> getByPartnerPaged(Long personId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null) {
            return consumptionRepo.findByPartnerPersonIdAndConsumptionOpeningBetween(personId, from, to, pageable);
        }
        return consumptionRepo.findByPartnerPersonId(personId, pageable);
    }

    public List<PartnerConsumption> getByPartnerId(Long partnerPersonId) {
        List<PartnerConsumption> list = consumptionRepo.findByPartnerPersonId(partnerPersonId);
        return list.isEmpty() ? null : list;
    }

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

    private double safe(Double v) {
        return v != null ? v : 0.0;
    }
}
