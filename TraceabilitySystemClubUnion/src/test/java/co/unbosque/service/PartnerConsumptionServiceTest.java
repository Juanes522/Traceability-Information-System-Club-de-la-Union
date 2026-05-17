package co.unbosque.service;

import co.unbosque.dto.ConsumptionCreateRequest;
import co.unbosque.model.Notification;
import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
import co.unbosque.repository.NotificationRepository;
import co.unbosque.repository.PartnerConsumptionRepository;
import co.unbosque.repository.PersonPartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PartnerConsumptionServiceTest {

    private PartnerConsumptionRepository consumptionRepo;
    private PersonPartnerRepository partnerRepo;
    private NotificationRepository notRepo;
    private PushNotificationService pushService;
    private EmailService emailService;
    private PartnerConsumptionService service;

    @BeforeEach
    void setUp() {
        consumptionRepo = mock(PartnerConsumptionRepository.class);
        partnerRepo     = mock(PersonPartnerRepository.class);
        notRepo         = mock(NotificationRepository.class);
        pushService     = mock(PushNotificationService.class);
        emailService    = mock(EmailService.class);
        service = new PartnerConsumptionService(
                consumptionRepo, partnerRepo, notRepo, pushService, emailService);
    }

    @Test
    void register_callsEmailService_afterSavingConsumption() {
        ConsumptionCreateRequest req = new ConsumptionCreateRequest();
        req.setPartnerId(1L);
        req.setEnviroment("Restaurante");
        req.setAccount(101);
        req.setTable("3");
        req.setWaiterName("Pedro");
        req.setIsPartner('S');
        req.setConsumptionValue(40000.0);
        req.setIva(7600.0);
        req.setService(4000.0);
        req.setTip(1000.0);

        PersonPartner partner = new PersonPartner();
        partner.setIdentification("900000");
        partner.setFirstName("Laura");
        partner.setShareNumber(7L);
        partner.setEmail(new String[]{"laura@example.com"});

        when(partnerRepo.findByPersonId(1L)).thenReturn(Optional.of(partner));
        when(consumptionRepo.save(any(PartnerConsumption.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notRepo.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.register(req);

        verify(emailService, times(1))
                .sendConsumptionNotificationEmail(eq(partner), any(PartnerConsumption.class), eq(52600.0));
    }

    @Test
    void register_stillSavesConsumption_whenPartnerHasNoEmail() {
        ConsumptionCreateRequest req = new ConsumptionCreateRequest();
        req.setPartnerId(2L);
        req.setEnviroment("Bar");
        req.setAccount(202);
        req.setTable("1");
        req.setWaiterName("Ana");
        req.setIsPartner('S');
        req.setConsumptionValue(10000.0);
        req.setIva(1900.0);
        req.setService(1000.0);
        req.setTip(0.0);

        PersonPartner partnerNoEmail = new PersonPartner();
        partnerNoEmail.setIdentification("800000");
        partnerNoEmail.setEmail(new String[]{});

        when(partnerRepo.findByPersonId(2L)).thenReturn(Optional.of(partnerNoEmail));
        when(consumptionRepo.save(any(PartnerConsumption.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notRepo.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.register(req);

        verify(consumptionRepo, times(1)).save(any(PartnerConsumption.class));
    }
}
