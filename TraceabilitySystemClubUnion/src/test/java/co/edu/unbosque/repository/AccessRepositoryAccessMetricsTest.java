package co.edu.unbosque.repository;

import co.edu.unbosque.model.Access;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import co.edu.unbosque.security.AesGcmEncryptionService;
import co.edu.unbosque.security.DeterministicEncryptionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({ AesGcmEncryptionService.class, DeterministicEncryptionService.class })
class AccessRepositoryAccessMetricsTest {

	@Autowired private TestEntityManager em;
	@Autowired private AccessRepository accessRepo;
	@Autowired private PartnerConsumptionRepository consumptionRepo;

	private PersonPartner partner(String id) {
		PersonPartner p = new PersonPartner();
		p.setIdentification(id);
		return em.persistAndFlush(p);
	}

	private Access access(PersonPartner p, LocalDateTime adm, LocalDateTime dep) {
		Access a = new Access();
		a.setPartner(p);
		a.setDateTimeAdmission(adm);
		a.setDateTimeDeparture(dep);
		return a;
	}

	@Test
	void countByDateTimeDepartureIsNull_countsOpenAccesses() {
		PersonPartner p1 = partner("1");
		PersonPartner p2 = partner("2");
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(access(p1, base, null));
		em.persist(access(p2, base, base.plusHours(2)));
		em.flush();
		assertEquals(1, accessRepo.countByDateTimeDepartureIsNull());
	}

	@Test
	void countDistinctPartnersInRange_countsUniquePartners() {
		PersonPartner p1 = partner("1");
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(access(p1, base, base.plusHours(1)));
		em.persist(access(p1, base.plusDays(1), base.plusDays(1).plusHours(1)));
		em.flush();
		assertEquals(1, accessRepo.countDistinctPartnersInRange(base.minusDays(1), base.plusDays(2)));
		assertEquals(2, accessRepo.countByDateTimeAdmissionBetween(base.minusDays(1), base.plusDays(2)));
	}

	@Test
	void closeOpenAccesses_setsDeparture() {
		PersonPartner p1 = partner("1");
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(access(p1, base, null));
		em.flush();
		int closed = accessRepo.closeOpenAccesses(base.plusHours(14));
		em.clear();
		assertEquals(1, closed);
		assertEquals(0, accessRepo.countByDateTimeDepartureIsNull());
	}

	@Test
	void occupancyByEnvironment_countsDistinctPartnersPerEnvironment() {
		PersonPartner p1 = partner("1");
		PersonPartner p2 = partner("2");
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(consumption(p1, "Bar", base));
		em.persist(consumption(p1, "Bar", base.plusHours(1)));
		em.persist(consumption(p2, "Bar", base.plusHours(2)));
		em.persist(consumption(p1, "Restaurante", base.plusHours(3)));
		em.flush();
		List<Object[]> rows = consumptionRepo.occupancyByEnvironment(base.minusHours(1), base.plusHours(5));
		assertEquals("Bar", rows.get(0)[0]);
		assertEquals(2L, ((Number) rows.get(0)[1]).longValue());
	}

	private PartnerConsumption consumption(PersonPartner p, String env, LocalDateTime opening) {
		PartnerConsumption c = new PartnerConsumption();
		c.setPartner(p);
		c.setEnviroment(env);
		c.setConsumptionValue(1.0);
		c.setIva(0.0);
		c.setService(0.0);
		c.setTip(0.0);
		c.setConsumptionOpening(opening);
		return c;
	}
}
