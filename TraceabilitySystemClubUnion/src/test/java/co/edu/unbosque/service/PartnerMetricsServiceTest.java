package co.edu.unbosque.service;

import co.edu.unbosque.dto.PartnerMetricsDTO;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.repository.AccessRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PartnerMetricsServiceTest {

	private PartnerConsumptionRepository repo;
	private AccessRepository accessRepo;
	private PartnerMetricsService service;
	private final LocalDateTime from = LocalDateTime.of(2026, 8, 10, 0, 0);
	private final LocalDateTime to = LocalDateTime.of(2026, 8, 12, 23, 0);

	@BeforeEach
	void setUp() {
		repo = mock(PartnerConsumptionRepository.class);
		accessRepo = mock(AccessRepository.class);
		service = new PartnerMetricsService(repo, accessRepo);
	}

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
	void forPartner_computesSummaryEnvironmentAndFilledTrend() {
		when(repo.findByPartnerPersonIdAndConsumptionOpeningBetween(7L, from, to)).thenReturn(List.of(
				c("Bar", 100.0, LocalDateTime.of(2026, 8, 10, 9, 0)),
				c("Restaurante", 300.0, LocalDateTime.of(2026, 8, 10, 20, 0)),
				c("Bar", 50.0, LocalDateTime.of(2026, 8, 12, 12, 0))
		));
		when(accessRepo.countByPartnerPersonIdAndDateTimeAdmissionBetween(7L, from, to)).thenReturn(4L);
		co.edu.unbosque.model.Access last = new co.edu.unbosque.model.Access();
		last.setDateTimeAdmission(LocalDateTime.of(2026, 8, 12, 19, 0));
		when(accessRepo.findFirstByPartnerPersonIdOrderByDateTimeAdmissionDesc(7L)).thenReturn(java.util.Optional.of(last));

		PartnerMetricsDTO dto = service.forPartner(7L, from, to, "day");

		assertEquals(4L, dto.getVisits());
		assertEquals("2026-08-12 19:00", dto.getLastVisit());
		assertEquals(450.0, dto.getSummary().getTotalBilled(), 0.001);
		assertEquals(3L, dto.getSummary().getChargeCount());
		assertEquals(150.0, dto.getSummary().getAveragePerAccount(), 0.001);
		assertEquals("Restaurante", dto.getByEnvironment().get(0).getEnvironment());
		assertEquals(300.0, dto.getByEnvironment().get(0).getTotal(), 0.001);
		assertEquals(List.of("2026-08-10", "2026-08-11", "2026-08-12"),
				dto.getTrend().stream().map(t -> t.getBucket()).toList());
		assertEquals(0.0, dto.getTrend().get(1).getTotal(), 0.001);
	}

	@Test
	void forPartner_emptyRowsGivesZeros() {
		when(repo.findByPartnerPersonIdAndConsumptionOpeningBetween(any(), any(), any())).thenReturn(List.of());
		when(accessRepo.countByPartnerPersonIdAndDateTimeAdmissionBetween(any(), any(), any())).thenReturn(0L);
		when(accessRepo.findFirstByPartnerPersonIdOrderByDateTimeAdmissionDesc(any())).thenReturn(java.util.Optional.empty());
		PartnerMetricsDTO dto = service.forPartner(7L, from, to, "day");
		assertEquals(0.0, dto.getSummary().getTotalBilled(), 0.001);
		assertEquals(0, dto.getByEnvironment().size());
	}

	@Test
	void forPartner_rejectsInvalidGranularity() {
		when(repo.findByPartnerPersonIdAndConsumptionOpeningBetween(any(), any(), any())).thenReturn(List.of());
		assertThrows(IllegalArgumentException.class, () -> service.forPartner(7L, from, to, "year"));
	}
}
