package co.edu.unbosque.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.service.AccessMetricsService;

@RestController
@RequestMapping("/metrics/access")
public class AccessMetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final AccessMetricsService metrics;

	public AccessMetricsController(AccessMetricsService metrics) {
		this.metrics = metrics;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/summary")
	public ResponseEntity<?> summary(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		return ResponseEntity.ok(metrics.summary(range[0], range[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/occupancy")
	public ResponseEntity<?> occupancy() {
		return ResponseEntity.ok(metrics.occupancyToday());
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/attendance")
	public ResponseEntity<?> attendance(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "day") String granularity) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		try {
			return ResponseEntity.ok(metrics.attendance(range[0], range[1], granularity));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	private LocalDateTime[] resolve(LocalDateTime from, LocalDateTime to) {
		LocalDateTime effectiveTo = to != null ? to : LocalDateTime.now();
		LocalDateTime effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
		if (effectiveFrom.isAfter(effectiveTo)
				|| ChronoUnit.DAYS.between(effectiveFrom, effectiveTo) > MAX_RANGE_DAYS) {
			return null;
		}
		return new LocalDateTime[] { effectiveFrom, effectiveTo };
	}

	private ResponseEntity<?> rangeError() {
		return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 1 año"));
	}
}
