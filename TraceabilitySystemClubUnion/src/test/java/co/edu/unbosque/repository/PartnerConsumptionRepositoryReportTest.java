package co.edu.unbosque.repository;

import co.edu.unbosque.model.PartnerConsumption;
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
/**
 * Verifica las consultas sin paginar que alimentan los reportes en PDF.
 *
 * <p>Comprueba el filtrado por rango y por ambiente. Son las consultas que recuperan el periodo completo, sin tope, de modo
 * que su corrección determina tanto el contenido del reporte como su costo.
 */
class PartnerConsumptionRepositoryReportTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private PartnerConsumptionRepository repository;

	private PartnerConsumption c(String env, double value, LocalDateTime opening) {
		PartnerConsumption p = new PartnerConsumption();
		p.setEnviroment(env);
		p.setConsumptionValue(value);
		p.setIva(0.0);
		p.setService(0.0);
		p.setTip(0.0);
		p.setConsumptionOpening(opening);
		return p;
	}

	@Test
	void findByConsumptionOpeningBetween_returnsRowsInRange() {
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(c("Bar", 100.0, base));
		em.persist(c("Restaurante", 50.0, base.minusMonths(2)));
		em.flush();

		List<PartnerConsumption> rows = repository.findByConsumptionOpeningBetween(base.minusDays(1), base.plusDays(1));

		assertEquals(1, rows.size());
		assertEquals("Bar", rows.get(0).getEnviroment());
	}

	@Test
	void findByEnviromentAndConsumptionOpeningBetween_filtersByEnvironment() {
		LocalDateTime base = LocalDateTime.of(2026, 8, 10, 12, 0);
		em.persist(c("Bar", 100.0, base));
		em.persist(c("Restaurante", 200.0, base));
		em.flush();

		List<PartnerConsumption> rows = repository.findByEnviromentAndConsumptionOpeningBetween("Bar", base.minusDays(1), base.plusDays(1));

		assertEquals(1, rows.size());
		assertEquals("Bar", rows.get(0).getEnviroment());
	}
}
