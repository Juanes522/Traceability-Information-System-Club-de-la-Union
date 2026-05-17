package co.unbosque.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unbosque.dto.ConsumptionCreateRequest;
import co.unbosque.dto.NotificationDTO;
import co.unbosque.model.Notification;
import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
import co.unbosque.repository.NotificationRepository;
import co.unbosque.repository.PartnerConsumptionRepository;
import co.unbosque.repository.PersonPartnerRepository;

@Service
@Transactional
public class PartnerConsumptionService {

    private final PartnerConsumptionRepository consumptionRepo;
    private final PersonPartnerRepository partnerRepo;
    private final NotificationRepository notRepo;
    private final PushNotificationService pushService;
    private final EmailService emailService;

    public PartnerConsumptionService(PartnerConsumptionRepository consumptionRepo,
                                     PersonPartnerRepository partnerRepo,
                                     NotificationRepository notRepo,
                                     PushNotificationService pushService,
                                     EmailService emailService) {
        this.consumptionRepo = consumptionRepo;
        this.partnerRepo     = partnerRepo;
        this.notRepo         = notRepo;
        this.pushService     = pushService;
        this.emailService    = emailService;
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
                        ? req.getConsumptionOpening().atStartOfDay()
                        : LocalDateTime.now());
        LocalDateTime opening = consumption.getConsumptionOpening();
        consumption.setConsumptionClosing(
                req.getConsumptionClosing() != null
                        ? req.getConsumptionClosing().atStartOfDay()
                        : opening.plusMinutes(20));

        PartnerConsumption saved = consumptionRepo.save(consumption);

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

        return saved;
    }

    public List<PartnerConsumption> getByEnviroment(String enviroment) {
        List<PartnerConsumption> list = consumptionRepo.findByEnviroment(enviroment);
        return list.isEmpty() ? null : list;
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

    private double safe(Double v) {
        return v != null ? v : 0.0;
    }
}
