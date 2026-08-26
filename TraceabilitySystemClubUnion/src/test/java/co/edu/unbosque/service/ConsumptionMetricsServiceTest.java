package co.edu.unbosque.service;

import co.edu.unbosque.dto.ConsumptionSummaryDTO;
import co.edu.unbosque.dto.EnvironmentTotalDTO;
import co.edu.unbosque.repository.PartnerConsumptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsumptionMetricsServiceTest {

	private PartnerConsumptionRepository repo;
	private ConsumptionMetricsService service;
	private final LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
	private final LocalDateTime to = LocalDateTime.of(2026, 8, 31, 0, 0);

	@BeforeEach
	void setUp() {
		repo = mock(PartnerConsumptionRepository.class);
		service = new ConsumptionMetricsService(repo);
	}

	@Test
	void summary_computesTotalsAverageAndTipPercentage() {
		when(repo.aggregateSummary(from, to))
				.thenReturn(List.<Object[]>of(new Object[]{300.0, 57.0, 30.0, 15.0, 3L}));

		ConsumptionSummaryDTO dto = service.summary(from, to);

		assertEquals(402.0, dto.getTotalBilled(), 0.001);
		assertEquals(300.0, dto.getTotalConsumption(), 0.001);
		assertEquals(3L, dto.getChargeCount());
		assertEquals(134.0, dto.getAveragePerAccount(), 0.001);
		assertEquals(5.0, dto.getTipPercentage(), 0.001);
	}

	@Test
	void summary_handlesZeroChargesWithoutDivisionError() {
		when(repo.aggregateSummary(from, to))
				.thenReturn(List.<Object[]>of(new Object[]{0.0, 0.0, 0.0, 0.0, 0L}));

		ConsumptionSummaryDTO dto = service.summary(from, to);

		assertEquals(0.0, dto.getAveragePerAccount(), 0.001);
		assertEquals(0.0, dto.getTipPercentage(), 0.001);
	}

	@Test
	void byEnvironment_computesPercentageOfGlobalTotal() {
		when(repo.aggregateByEnvironment(from, to)).thenReturn(List.of(
				new Object[]{"Restaurante", 300.0, 2L},
				new Object[]{"Bar", 100.0, 1L}
		));

		List<EnvironmentTotalDTO> result = service.byEnvironment(from, to);

		assertEquals(2, result.size());
		assertEquals("Restaurante", result.get(0).getEnvironment());
		assertEquals(75.0, result.get(0).getPercentage(), 0.001);
		assertEquals(25.0, result.get(1).getPercentage(), 0.001);
	}

	@Test
	void byEnvironment_emptyWhenNoData() {
		when(repo.aggregateByEnvironment(any(), any())).thenReturn(List.of());
		assertEquals(0, service.byEnvironment(from, to).size());
	}

	private static co.edu.unbosque.dto.ConsumptionRowView view(LocalDateTime opening, double value) {
		return new co.edu.unbosque.dto.ConsumptionRowView() {
			public LocalDateTime getConsumptionOpening() { return opening; }
			public Double getConsumptionValue() { return value; }
			public Double getIva() { return 0.0; }
			public Double getService() { return 0.0; }
			public Double getTip() { return 0.0; }
		};
	}

	@Test
	void trend_groupsByDay() {
		LocalDateTime f = LocalDateTime.of(2026, 8, 10, 0, 0);
		LocalDateTime t = LocalDateTime.of(2026, 8, 11, 23, 0);
		when(repo.findRowsInRange(f, t)).thenReturn(List.of(
				view(LocalDateTime.of(2026, 8, 10, 9, 0), 100.0),
				view(LocalDateTime.of(2026, 8, 10, 20, 0), 50.0),
				view(LocalDateTime.of(2026, 8, 11, 12, 0), 30.0)
		));

		List<co.edu.unbosque.dto.TrendPointDTO> trend = service.trend(f, t, "day");

		assertEquals(2, trend.size());
		assertEquals("2026-08-10", trend.get(0).getBucket());
		assertEquals(150.0, trend.get(0).getTotal(), 0.001);
		assertEquals(2L, trend.get(0).getCount());
		assertEquals("2026-08-11", trend.get(1).getBucket());
	}

	@Test
	void trend_fillsEmptyDayBucketsWithZero() {
		LocalDateTime f = LocalDateTime.of(2026, 8, 10, 0, 0);
		LocalDateTime t = LocalDateTime.of(2026, 8, 12, 23, 0);
		when(repo.findRowsInRange(f, t)).thenReturn(List.of(
				view(LocalDateTime.of(2026, 8, 10, 9, 0), 100.0),
				view(LocalDateTime.of(2026, 8, 12, 9, 0), 30.0)
		));

		List<co.edu.unbosque.dto.TrendPointDTO> trend = service.trend(f, t, "day");

		assertEquals(List.of("2026-08-10", "2026-08-11", "2026-08-12"),
				trend.stream().map(co.edu.unbosque.dto.TrendPointDTO::getBucket).toList());
		assertEquals(0.0, trend.get(1).getTotal(), 0.001);
		assertEquals(0L, trend.get(1).getCount());
	}

	@Test
	void trend_fillsEmptyMonthBuckets() {
		LocalDateTime f = LocalDateTime.of(2026, 6, 5, 0, 0);
		LocalDateTime t = LocalDateTime.of(2026, 8, 5, 0, 0);
		when(repo.findRowsInRange(f, t)).thenReturn(List.of(
				view(LocalDateTime.of(2026, 6, 6, 9, 0), 100.0),
				view(LocalDateTime.of(2026, 8, 1, 9, 0), 40.0)
		));

		List<co.edu.unbosque.dto.TrendPointDTO> trend = service.trend(f, t, "month");

		assertEquals(List.of("2026-06", "2026-07", "2026-08"),
				trend.stream().map(co.edu.unbosque.dto.TrendPointDTO::getBucket).toList());
		assertEquals(0.0, trend.get(1).getTotal(), 0.001);
	}

	@Test
	void trend_rejectsInvalidGranularity() {
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
				() -> service.trend(from, to, "year"));
	}

	@Test
	void peak_distributesByHourAndWeekday() {
		when(repo.findRowsInRange(from, to)).thenReturn(List.of(
				view(LocalDateTime.of(2026, 8, 10, 9, 0), 100.0),
				view(LocalDateTime.of(2026, 8, 17, 9, 30), 50.0)
		));

		co.edu.unbosque.dto.PeakDTO peak = service.peak(from, to);

		assertEquals(24, peak.getByHour().size());
		assertEquals(7, peak.getByWeekday().size());
		assertEquals(150.0, peak.getByHour().get(9).getTotal(), 0.001);
		assertEquals(2L, peak.getByHour().get(9).getCount());
		assertEquals("MONDAY", peak.getByWeekday().get(0).getWeekday());
		assertEquals(150.0, peak.getByWeekday().get(0).getTotal(), 0.001);
	}

	@Test
	void comparison_computesVarianceBetweenMonths() {
		LocalDateTime curFrom = LocalDateTime.of(2026, 8, 1, 0, 0);
		LocalDateTime curTo = LocalDateTime.of(2026, 9, 1, 0, 0);
		LocalDateTime prevFrom = LocalDateTime.of(2026, 7, 1, 0, 0);
		LocalDateTime prevTo = LocalDateTime.of(2026, 8, 1, 0, 0);
		when(repo.aggregateSummary(curFrom, curTo)).thenReturn(List.<Object[]>of(new Object[]{200.0, 0.0, 0.0, 0.0, 4L}));
		when(repo.aggregateSummary(prevFrom, prevTo)).thenReturn(List.<Object[]>of(new Object[]{100.0, 0.0, 0.0, 0.0, 2L}));

		co.edu.unbosque.dto.ComparisonDTO dto = service.comparison(java.time.YearMonth.of(2026, 8));

		assertEquals(200.0, dto.getCurrentTotal(), 0.001);
		assertEquals(100.0, dto.getPreviousTotal(), 0.001);
		assertEquals(100.0, dto.getVariancePercentage(), 0.001);
	}

	@Test
	void comparison_zeroPreviousGivesZeroVariance() {
		LocalDateTime curFrom = LocalDateTime.of(2026, 8, 1, 0, 0);
		LocalDateTime curTo = LocalDateTime.of(2026, 9, 1, 0, 0);
		LocalDateTime prevFrom = LocalDateTime.of(2026, 7, 1, 0, 0);
		LocalDateTime prevTo = LocalDateTime.of(2026, 8, 1, 0, 0);
		when(repo.aggregateSummary(curFrom, curTo)).thenReturn(List.<Object[]>of(new Object[]{50.0, 0.0, 0.0, 0.0, 1L}));
		when(repo.aggregateSummary(prevFrom, prevTo)).thenReturn(List.<Object[]>of(new Object[]{0.0, 0.0, 0.0, 0.0, 0L}));

		co.edu.unbosque.dto.ComparisonDTO dto = service.comparison(java.time.YearMonth.of(2026, 8));

		assertEquals(0.0, dto.getVariancePercentage(), 0.001);
	}

	@Test
	void peakHeatmap_groupsByWeekdayAndHour() {
		when(repo.findRowsInRange(from, to)).thenReturn(List.of(
				view(LocalDateTime.of(2026, 8, 10, 9, 0), 100.0),
				view(LocalDateTime.of(2026, 8, 10, 9, 30), 50.0),
				view(LocalDateTime.of(2026, 8, 11, 20, 0), 30.0)
		));

		List<co.edu.unbosque.dto.PeakHeatmapCellDTO> cells = service.peakHeatmap(from, to);

		co.edu.unbosque.dto.PeakHeatmapCellDTO monday9 = cells.stream()
				.filter(c -> c.getWeekday() == 0 && c.getHour() == 9).findFirst().orElseThrow();
		assertEquals(150.0, monday9.getTotal(), 0.001);
		assertEquals(2L, monday9.getCount());
		co.edu.unbosque.dto.PeakHeatmapCellDTO tuesday20 = cells.stream()
				.filter(c -> c.getWeekday() == 1 && c.getHour() == 20).findFirst().orElseThrow();
		assertEquals(30.0, tuesday20.getTotal(), 0.001);
	}
}
