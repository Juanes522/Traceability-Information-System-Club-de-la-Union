package co.edu.unbosque.service;

import co.edu.unbosque.dto.AccessSummaryDTO;
import co.edu.unbosque.dto.ConsumptionSummaryDTO;
import co.edu.unbosque.dto.MonthlySnapshotDTO;
import co.edu.unbosque.model.MonthlySnapshot;
import co.edu.unbosque.repository.MonthlySnapshotRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SnapshotServiceTest {

	private ConsumptionMetricsService consumptionMetrics;
	private AccessMetricsService accessMetrics;
	private MonthlySnapshotRepository snapshotRepo;
	private PartnerConsumptionRepository consumptionRepo;
	private SnapshotService service;

	@BeforeEach
	void setUp() {
		consumptionMetrics = mock(ConsumptionMetricsService.class);
		accessMetrics = mock(AccessMetricsService.class);
		snapshotRepo = mock(MonthlySnapshotRepository.class);
		consumptionRepo = mock(PartnerConsumptionRepository.class);
		service = new SnapshotService(consumptionMetrics, accessMetrics, snapshotRepo, consumptionRepo);
	}

	private ConsumptionSummaryDTO cs() {
		ConsumptionSummaryDTO c = new ConsumptionSummaryDTO();
		c.setTotalBilled(500.0);
		c.setChargeCount(4);
		c.setAveragePerAccount(125.0);
		c.setTipPercentage(3.0);
		return c;
	}

	private AccessSummaryDTO as() {
		AccessSummaryDTO a = new AccessSummaryDTO();
		a.setVisits(6);
		a.setUniquePartners(3);
		return a;
	}

	@Test
	void snapshotMonth_upsertsWithComputedMetrics() {
		when(consumptionMetrics.summary(any(), any())).thenReturn(cs());
		when(accessMetrics.summary(any(), any())).thenReturn(as());
		when(snapshotRepo.findByYearMonth("2026-07")).thenReturn(Optional.empty());

		service.snapshotMonth(YearMonth.of(2026, 7));

		ArgumentCaptor<MonthlySnapshot> cap = ArgumentCaptor.forClass(MonthlySnapshot.class);
		verify(snapshotRepo).save(cap.capture());
		MonthlySnapshot s = cap.getValue();
		assertEquals("2026-07", s.getYearMonth());
		assertEquals(500.0, s.getTotalBilled(), 0.001);
		assertEquals(4L, s.getChargeCount());
		assertEquals(6L, s.getVisits());
		assertEquals(3L, s.getUniquePartners());
	}

	@Test
	void snapshotMonth_reusesExistingRowForUpsert() {
		MonthlySnapshot existing = new MonthlySnapshot();
		existing.setYearMonth("2026-07");
		existing.setId(9L);
		when(consumptionMetrics.summary(any(), any())).thenReturn(cs());
		when(accessMetrics.summary(any(), any())).thenReturn(as());
		when(snapshotRepo.findByYearMonth("2026-07")).thenReturn(Optional.of(existing));

		service.snapshotMonth(YearMonth.of(2026, 7));

		ArgumentCaptor<MonthlySnapshot> cap = ArgumentCaptor.forClass(MonthlySnapshot.class);
		verify(snapshotRepo).save(cap.capture());
		assertEquals(9L, cap.getValue().getId());
	}

	@Test
	void backfillMissing_returnsWhenNoData() {
		when(consumptionRepo.findEarliestConsumption()).thenReturn(null);
		service.backfillMissing();
		verify(snapshotRepo, never()).save(any());
	}

	@Test
	void backfillMissing_snapshotsMissingMonths() {
		when(consumptionRepo.findEarliestConsumption()).thenReturn(LocalDateTime.now().minusMonths(2).withDayOfMonth(1));
		when(snapshotRepo.existsByYearMonth(any())).thenReturn(false);
		when(consumptionMetrics.summary(any(), any())).thenReturn(new ConsumptionSummaryDTO());
		when(accessMetrics.summary(any(), any())).thenReturn(new AccessSummaryDTO());
		service.backfillMissing();
		verify(snapshotRepo, atLeastOnce()).save(any());
	}

	@Test
	void backfillMissing_skipsExistingMonths() {
		when(consumptionRepo.findEarliestConsumption()).thenReturn(LocalDateTime.now().minusMonths(2).withDayOfMonth(1));
		when(snapshotRepo.existsByYearMonth(any())).thenReturn(true);
		service.backfillMissing();
		verify(snapshotRepo, never()).save(any());
	}

	@Test
	void list_mapsToDto() {
		MonthlySnapshot s = new MonthlySnapshot();
		s.setYearMonth("2026-07");
		s.setTotalBilled(500.0);
		when(snapshotRepo.findAllByOrderByYearMonthAsc()).thenReturn(List.of(s));
		List<MonthlySnapshotDTO> dtos = service.list();
		assertEquals("2026-07", dtos.get(0).getYearMonth());
		assertEquals(500.0, dtos.get(0).getTotalBilled(), 0.001);
	}
}
