package co.unbosque.repository;

import co.unbosque.model.Access;
import co.unbosque.model.PersonPartner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AccessRepositoryTest {

	@Autowired
	private AccessRepository accessRepository;

	@Autowired
	private PersonPartnerRepository partnerRepository;

	private final LocalDateTime now = LocalDateTime.now();
	private Long presentPartnerId;
	private Long absentPartnerId;

	@BeforeEach
	void setUp() {
		accessRepository.deleteAll();
		partnerRepository.deleteAll();

		PersonPartner present = partner("acc-present-" + System.nanoTime());
		PersonPartner absent = partner("acc-absent-" + System.nanoTime());
		partnerRepository.save(present);
		partnerRepository.save(absent);
		presentPartnerId = present.getPersonId();
		absentPartnerId = absent.getPersonId();

		accessRepository.save(access(present, now.minusDays(1), now.minusDays(1).plusHours(2)));
		accessRepository.save(access(present, now, null));
		accessRepository.save(access(present, now.minusDays(3), now.minusDays(3).plusHours(1)));
		accessRepository.save(access(absent, now.minusHours(5), now.minusHours(4)));
	}

	private PersonPartner partner(String identification) {
		PersonPartner p = new PersonPartner();
		p.setIdentification(identification);
		return p;
	}

	private Access access(PersonPartner partner, LocalDateTime admission, LocalDateTime departure) {
		Access a = new Access();
		a.setPartner(partner);
		a.setDateTimeAdmission(admission);
		a.setDateTimeDeparture(departure);
		return a;
	}

	@Test
	void findByPartnerOrderByAdmissionDesc_returnsOnlyThatPartnerNewestFirst() {
		List<Access> history = accessRepository
				.findByPartnerPersonIdOrderByDateTimeAdmissionDesc(presentPartnerId);

		assertEquals(3, history.size());
		assertTrue(history.get(0).getDateTimeAdmission()
				.isAfter(history.get(1).getDateTimeAdmission()));
		assertTrue(history.get(1).getDateTimeAdmission()
				.isAfter(history.get(2).getDateTimeAdmission()));
	}

	@Test
	void findOpenAccessByPartnerId_returnsTheAccessWithoutDeparture() {
		Optional<Access> open = accessRepository.findOpenAccessByPartnerId(presentPartnerId);

		assertTrue(open.isPresent());
		assertEquals(null, open.get().getDateTimeDeparture());
	}

	@Test
	void isPartnerCurrentlyPresent_trueWhenOpenAccessExists_falseOtherwise() {
		assertTrue(accessRepository.isPartnerCurrentlyPresent(presentPartnerId));
		assertFalse(accessRepository.isPartnerCurrentlyPresent(absentPartnerId));
	}

	@Test
	void findByAdmissionBetween_returnsOnlyAccessesInsideRange() {
		List<Access> inRange = accessRepository.findByPartnerPersonIdAndDateTimeAdmissionBetween(
				presentPartnerId, now.minusDays(2), now.plusHours(1));

		assertEquals(2, inRange.size());
	}
}
