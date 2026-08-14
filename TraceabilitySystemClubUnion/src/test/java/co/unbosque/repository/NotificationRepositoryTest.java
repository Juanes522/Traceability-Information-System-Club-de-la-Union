package co.unbosque.repository;

import co.unbosque.model.Notification;
import co.unbosque.model.PartnerConsumption;
import co.unbosque.model.PersonPartner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class NotificationRepositoryTest {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private PartnerConsumptionRepository consumptionRepository;

	@Autowired
	private PersonPartnerRepository partnerRepository;

	private final LocalDateTime now = LocalDateTime.now();
	private String identification;
	private Long consumptionId;

	@BeforeEach
	void setUp() {
		notificationRepository.deleteAll();
		consumptionRepository.deleteAll();
		partnerRepository.deleteAll();

		identification = "notif-" + System.nanoTime();
		PersonPartner partner = new PersonPartner();
		partner.setIdentification(identification);
		partnerRepository.save(partner);

		PartnerConsumption consumption = consumption(partner);
		consumptionRepository.save(consumption);
		consumptionId = consumption.getConsumptionId();

		notificationRepository.save(notification(consumption, now));
		notificationRepository.save(notification(consumption, now.minusHours(1)));

		PersonPartner other = new PersonPartner();
		other.setIdentification("other-" + System.nanoTime());
		partnerRepository.save(other);
		PartnerConsumption otherConsumption = consumption(other);
		consumptionRepository.save(otherConsumption);
		notificationRepository.save(notification(otherConsumption, now));
	}

	private PartnerConsumption consumption(PersonPartner partner) {
		PartnerConsumption c = new PartnerConsumption();
		c.setPartner(partner);
		c.setEnviroment("Restaurante");
		c.setConsumptionOpening(now);
		return c;
	}

	private Notification notification(PartnerConsumption consumption, LocalDateTime generationDate) {
		Notification n = new Notification();
		n.setConsumption(consumption);
		n.setNotificationType("CHARGE_NOTIFICATION");
		n.setTitle("Nuevo cargo");
		n.setBody("Cuerpo");
		n.setGenerationDate(generationDate);
		n.setState('S');
		return n;
	}

	@Test
	void findByPartnerIdentificationOrderByGenerationDateDesc_returnsOnlyThatPartnerNewestFirst() {
		List<Notification> result = notificationRepository
				.findByConsumptionPartnerIdentificationOrderByGenerationDateDesc(identification);

		assertEquals(2, result.size());
		assertTrue(result.get(0).getGenerationDate().isAfter(result.get(1).getGenerationDate()));
	}

	@Test
	void findByConsumptionId_returnsNotificationsForThatConsumption() {
		List<Notification> result = notificationRepository.findByConsumptionConsumptionId(consumptionId);

		assertEquals(2, result.size());
	}
}
