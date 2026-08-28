package co.edu.unbosque.service;

import co.edu.unbosque.dto.ConsumptionCreateRequest;
import co.edu.unbosque.model.AuditEventType;
import co.edu.unbosque.model.AuditResult;
import co.edu.unbosque.model.Notification;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.NotificationRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;
import co.edu.unbosque.repository.PersonPartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	private AccessService accessService;
	private PartnerConsumptionService service;

	@BeforeEach
	void setUp() {
		consumptionRepo = mock(PartnerConsumptionRepository.class);
		partnerRepo = mock(PersonPartnerRepository.class);
		notRepo = mock(NotificationRepository.class);
		pushService = mock(PushNotificationService.class);
		emailService = mock(EmailService.class);
		auditService = mock(AuditService.class);
		accessService = mock(AccessService.class);
		service = new PartnerConsumptionService(consumptionRepo, partnerRepo, notRepo,
				pushService, emailService, auditService, accessService);
	}

	@Test
	void register_usesConsumptionOpeningForNotificationAccessAndAudit() {
		PersonPartner partner = new PersonPartner();
		partner.setPersonId(5L);
		partner.setIdentification("999");
		when(partnerRepo.findByPersonId(5L)).thenReturn(Optional.of(partner));
		when(consumptionRepo.save(any())).thenAnswer(inv -> {
			PartnerConsumption c = inv.getArgument(0);
			c.setConsumptionId(42L);
			return c;
		});

		LocalDateTime opening = LocalDateTime.of(2026, 5, 15, 11, 45, 0);
		ConsumptionCreateRequest req = new ConsumptionCreateRequest();
		req.setPartnerId(5L);
		req.setEnviroment("Comedor Principal");
		req.setWaiterName("Ana");
		req.setConsumptionValue(30.0);
		req.setIva(3.6);
		req.setService(3.0);
		req.setTip(2.0);
		req.setConsumptionOpening(opening);

		service.register(req);

		ArgumentCaptor<Notification> nCap = ArgumentCaptor.forClass(Notification.class);
		verify(notRepo).save(nCap.capture());
		assertEquals(opening, nCap.getValue().getGenerationDate());

		verify(accessService).registerPresence(partner, opening);

		Instant expected = opening.atZone(ZoneId.systemDefault()).toInstant();
		verify(auditService).record(eq(AuditEventType.CHARGE_REGISTERED), eq(AuditResult.SUCCESS),
				eq("999"), any(), any(), eq("42"), eq(expected));
	}
}
