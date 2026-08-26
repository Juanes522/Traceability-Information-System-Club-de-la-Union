package co.edu.unbosque.controller;

import co.edu.unbosque.dto.ConsumptionSummaryDTO;
import co.edu.unbosque.dto.SecuritySummaryDTO;
import co.edu.unbosque.service.ConsumptionMetricsService;
import co.edu.unbosque.service.SecurityMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricsControllerTest {

	private ConsumptionMetricsService consumptionMetrics;
	private SecurityMetricsService securityMetrics;
	private MetricsController controller;

	@BeforeEach
	void setUp() {
		consumptionMetrics = mock(ConsumptionMetricsService.class);
		securityMetrics = mock(SecurityMetricsService.class);
		controller = new MetricsController(consumptionMetrics, securityMetrics);
	}

	@Test
	void summary_returnsOkWithBody() {
		when(consumptionMetrics.summary(any(), any())).thenReturn(new ConsumptionSummaryDTO());
		ResponseEntity<?> response = controller.consumptionSummary(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void summary_rejectsRangeWiderThanOneYear() {
		ResponseEntity<?> response = controller.consumptionSummary(
				LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2026, 6, 1, 0, 0));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void trend_rejectsInvalidGranularity() {
		when(consumptionMetrics.trend(any(), any(), org.mockito.ArgumentMatchers.eq("year")))
				.thenThrow(new IllegalArgumentException("bad"));
		ResponseEntity<?> response = controller.consumptionTrend(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0), "year");
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void securitySummary_returnsOkWithBody() {
		when(securityMetrics.summary(any(), any())).thenReturn(new SecuritySummaryDTO());
		ResponseEntity<?> response = controller.securitySummary(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void summary_rejectsInvertedRange() {
		ResponseEntity<?> response = controller.consumptionSummary(
				LocalDateTime.of(2026, 8, 31, 0, 0), LocalDateTime.of(2026, 8, 1, 0, 0));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void comparison_rejectsInvalidMonthFormat() {
		ResponseEntity<?> response = controller.consumptionComparison("not-a-month");
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void peakHeatmap_returnsOkWithBody() {
		when(consumptionMetrics.peakHeatmap(any(), any())).thenReturn(java.util.List.of());
		ResponseEntity<?> response = controller.consumptionPeakHeatmap(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void peakHeatmap_rejectsInvertedRange() {
		ResponseEntity<?> response = controller.consumptionPeakHeatmap(
				LocalDateTime.of(2026, 8, 31, 0, 0), LocalDateTime.of(2026, 8, 1, 0, 0));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
}
