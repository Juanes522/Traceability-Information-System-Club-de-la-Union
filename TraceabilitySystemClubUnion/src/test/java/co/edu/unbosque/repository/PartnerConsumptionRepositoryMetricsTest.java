package co.edu.unbosque.repository;

import co.edu.unbosque.dto.ConsumptionRowView;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.security.AesGcmEncryptionService;
import co.edu.unbosque.security.DeterministicEncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({ AesGcmEncryptionService.class, DeterministicEncryptionService.class })
class PartnerConsumptionRepositoryMetricsTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private PartnerConsumptionRepository repository;

	private PartnerConsumption consumption(String env, double value, double iva, double service, double tip, LocalDateTime opening) {
		PartnerConsumption c = new PartnerConsumption();
		c.setEnviroment(env);
		c.setConsumptionValue(value);
		c.setIva(iva);
		c.setService(service);
		c.setTip(tip);
		c.setConsumptionOpening(opening);
		return c;
	}

	@Test
	void aggregateSummary_sumsValuesAndCountsInRange() {
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(consumption("Bar", 100.0, 19.0, 10.0, 5.0, base));
		em.persist(consumption("Restaurante", 200.0, 38.0, 20.0, 0.0, base.plusHours(1)));
		em.persist(consumption("Bar", 50.0, 9.5, 5.0, 2.0, base.minusMonths(2)));
		em.flush();

		Object[] row = repository.aggregateSummary(base.minusDays(1), base.plusDays(1)).get(0);

		assertEquals(300.0, ((Number) row[0]).doubleValue(), 0.001);
		assertEquals(57.0, ((Number) row[1]).doubleValue(), 0.001);
		assertEquals(30.0, ((Number) row[2]).doubleValue(), 0.001);
		assertEquals(5.0, ((Number) row[3]).doubleValue(), 0.001);
		assertEquals(2L, ((Number) row[4]).longValue());
	}

	@Test
	void aggregateByEnvironment_groupsAndOrdersByTotalDesc() {
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(consumption("Bar", 100.0, 0.0, 0.0, 0.0, base));
		em.persist(consumption("Restaurante", 200.0, 0.0, 0.0, 0.0, base));
		em.persist(consumption("Restaurante", 50.0, 0.0, 0.0, 0.0, base));
		em.flush();

		List<Object[]> rows = repository.aggregateByEnvironment(base.minusDays(1), base.plusDays(1));

		assertEquals(2, rows.size());
		assertEquals("Restaurante", rows.get(0)[0]);
		assertEquals(250.0, ((Number) rows.get(0)[1]).doubleValue(), 0.001);
		assertEquals(2L, ((Number) rows.get(0)[2]).longValue());
		assertEquals("Bar", rows.get(1)[0]);
	}

	@Test
	void findRowsInRange_returnsOnlyRowsWithinRange() {
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(consumption("Bar", 100.0, 19.0, 10.0, 5.0, base));
		em.persist(consumption("Bar", 50.0, 9.5, 5.0, 2.0, base.minusMonths(2)));
		em.flush();

		List<ConsumptionRowView> rows = repository.findRowsInRange(base.minusDays(1), base.plusDays(1));

		assertEquals(1, rows.size());
		assertEquals(100.0, rows.get(0).getConsumptionValue(), 0.001);
		assertEquals(base, rows.get(0).getConsumptionOpening());
	}

	@Test
	void aggregateByEnvironment_includesRowsWithNullMoneyColumns() {
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		PartnerConsumption withNulls = new PartnerConsumption();
		withNulls.setEnviroment("Bar");
		withNulls.setConsumptionValue(100.0);
		withNulls.setIva(null);
		withNulls.setService(null);
		withNulls.setTip(null);
		withNulls.setConsumptionOpening(base);
		em.persist(withNulls);
		em.flush();

		List<Object[]> rows = repository.aggregateByEnvironment(base.minusDays(1), base.plusDays(1));

		assertEquals(1, rows.size());
		assertEquals("Bar", rows.get(0)[0]);
		assertEquals(100.0, ((Number) rows.get(0)[1]).doubleValue(), 0.001);
		assertEquals(1L, ((Number) rows.get(0)[2]).longValue());
	}
}
