package co.unbosque.service;

import co.unbosque.dto.ConsumptionCreateRequest;
import co.unbosque.dto.NotificationDTO;
import co.unbosque.model.Notification;
import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
import co.unbosque.repository.NotificationRepository;
import co.unbosque.repository.PartnerConsumptionRepository;
import co.unbosque.repository.PersonPartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PartnerConsumptionServiceTest {

    private PartnerConsumptionRepository consumptionRepo;
    private PersonPartnerRepository partnerRepo;
    private NotificationRepository notRepo;
    private PushNotificationService pushService;
    private EmailService emailService;
    private AuditService auditService;
    private PartnerConsumptionService service;

    @BeforeEach
    void setUp() {
        consumptionRepo = mock(PartnerConsumptionRepository.class);
        partnerRepo     = mock(PersonPartnerRepository.class);
        notRepo         = mock(NotificationRepository.class);
        pushService     = mock(PushNotificationService.class);
        emailService    = mock(EmailService.class);
        auditService    = mock(AuditService.class);
        service = new PartnerConsumptionService(
                consumptionRepo, partnerRepo, notRepo, pushService, emailService, auditService);
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

    @Test
    void getNotificationsForPartner_mapsToDtoWithComputedTotal() {
        PartnerConsumption consumption = new PartnerConsumption();
        consumption.setConsumptionId(55L);
        consumption.setEnviroment("Bar");
        consumption.setConsumptionValue(10000.0);
        consumption.setIva(1900.0);
        consumption.setService(1000.0);
        consumption.setTip(500.0);

        Notification notification = new Notification();
        notification.setNotificationId(1L);
        notification.setTitle("Nuevo cargo");
        notification.setBody("Cuerpo");
        notification.setGenerationDate(LocalDateTime.of(2026, 7, 8, 10, 0));
        notification.setState('S');
        notification.setConsumption(consumption);

        when(notRepo.findByConsumptionPartnerIdentificationOrderByGenerationDateDesc("900000"))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result = service.getNotificationsForPartner("900000");

        assertEquals(1, result.size());
        assertEquals(13400.0, result.get(0).getTotalAmount());
        assertEquals(55L, result.get(0).getConsumptionId());
        assertEquals("Bar", result.get(0).getEnvironment());
    }

    @Test
    void register_recordsChargeRegisteredAuditEvent() {
        ConsumptionCreateRequest req = new ConsumptionCreateRequest();
        req.setPartnerId(1L);
        req.setEnviroment("Restaurante");
        req.setTable("3");
        req.setWaiterName("Pedro");
        req.setConsumptionValue(40000.0);
        req.setIva(7600.0);
        req.setService(4000.0);
        req.setTip(1000.0);

        PersonPartner partner = new PersonPartner();
        partner.setIdentification("900000");
        when(partnerRepo.findByPersonId(1L)).thenReturn(Optional.of(partner));
        when(consumptionRepo.save(any(PartnerConsumption.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notRepo.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.register(req);

        verify(auditService).record(eq(co.unbosque.model.AuditEventType.CHARGE_REGISTERED),
                eq(co.unbosque.model.AuditResult.SUCCESS), eq("900000"),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getByEnviromentPaged_usesBetween_whenRangeGiven() {
        java.time.LocalDateTime from = java.time.LocalDateTime.of(2026, 8, 1, 0, 0);
        java.time.LocalDateTime to = java.time.LocalDateTime.of(2026, 8, 8, 0, 0);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(consumptionRepo.findByEnviromentAndConsumptionOpeningBetween("Bar", from, to, pageable)).thenReturn(page);

        org.junit.jupiter.api.Assertions.assertSame(page, service.getByEnviromentPaged("Bar", from, to, pageable));
    }

    @Test
    void getByEnviromentPaged_usesPlain_whenNoRange() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(consumptionRepo.findByEnviroment("Bar", pageable)).thenReturn(page);

        org.junit.jupiter.api.Assertions.assertSame(page, service.getByEnviromentPaged("Bar", null, null, pageable));
    }

    @Test
    void getByPartnerPaged_usesBetween_whenRangeGiven() {
        java.time.LocalDateTime from = java.time.LocalDateTime.of(2026, 8, 1, 0, 0);
        java.time.LocalDateTime to = java.time.LocalDateTime.of(2026, 8, 8, 0, 0);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(consumptionRepo.findByPartnerPersonIdAndConsumptionOpeningBetween(7L, from, to, pageable)).thenReturn(page);

        org.junit.jupiter.api.Assertions.assertSame(page, service.getByPartnerPaged(7L, from, to, pageable));
    }

    @Test
    void getByPartnerPaged_usesPlain_whenNoRange() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<PartnerConsumption> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(consumptionRepo.findByPartnerPersonId(7L, pageable)).thenReturn(page);

        org.junit.jupiter.api.Assertions.assertSame(page, service.getByPartnerPaged(7L, null, null, pageable));
    }
}
