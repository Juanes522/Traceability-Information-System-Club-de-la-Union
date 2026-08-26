package co.edu.unbosque.controller;

import co.edu.unbosque.dto.AccessSummaryDTO;
import co.edu.unbosque.service.AccessMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessMetricsControllerTest {

	private AccessMetricsService metrics;
	private AccessMetricsController controller;

	@BeforeEach
	void setUp() {
		metrics = mock(AccessMetricsService.class);
		controller = new AccessMetricsController(metrics);
	}

	@Test
	void summary_returnsOk() {
		when(metrics.summary(any(), any())).thenReturn(new AccessSummaryDTO());
		ResponseEntity<?> r = controller.summary(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	void summary_rejectsInvalidRange() {
		ResponseEntity<?> r = controller.summary(
				LocalDateTime.of(2026, 8, 31, 0, 0), LocalDateTime.of(2026, 8, 1, 0, 0));
		assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
	}

	@Test
	void occupancy_returnsOk() {
		when(metrics.occupancyToday()).thenReturn(java.util.List.of());
		ResponseEntity<?> r = controller.occupancy();
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	void attendance_rejectsInvalidGranularity() {
		when(metrics.attendance(any(), any(), org.mockito.ArgumentMatchers.eq("year")))
				.thenThrow(new IllegalArgumentException("bad"));
		ResponseEntity<?> r = controller.attendance(
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0), "year");
		assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
	}
}
