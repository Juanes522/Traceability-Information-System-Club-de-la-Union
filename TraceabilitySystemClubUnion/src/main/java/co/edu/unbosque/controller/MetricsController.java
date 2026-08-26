package co.edu.unbosque.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.service.ConsumptionMetricsService;
import co.edu.unbosque.service.SecurityMetricsService;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final ConsumptionMetricsService consumptionMetrics;
	private final SecurityMetricsService securityMetrics;

	public MetricsController(ConsumptionMetricsService consumptionMetrics, SecurityMetricsService securityMetrics) {
		this.consumptionMetrics = consumptionMetrics;
		this.securityMetrics = securityMetrics;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumption/summary")
	public ResponseEntity<?> consumptionSummary(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		return ResponseEntity.ok(consumptionMetrics.summary(range[0], range[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumption/by-environment")
	public ResponseEntity<?> consumptionByEnvironment(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		return ResponseEntity.ok(consumptionMetrics.byEnvironment(range[0], range[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumption/trend")
	public ResponseEntity<?> consumptionTrend(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "day") String granularity) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		try {
			return ResponseEntity.ok(consumptionMetrics.trend(range[0], range[1], granularity));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumption/comparison")
	public ResponseEntity<?> consumptionComparison(@RequestParam(required = false) String month) {
		YearMonth ym;
		try {
			ym = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
		} catch (DateTimeParseException e) {
			return ResponseEntity.badRequest().body(Map.of("message", "Formato de mes inválido (YYYY-MM)"));
		}
		return ResponseEntity.ok(consumptionMetrics.comparison(ym));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumption/peak")
	public ResponseEntity<?> consumptionPeak(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		return ResponseEntity.ok(consumptionMetrics.peak(range[0], range[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumption/peak-heatmap")
	public ResponseEntity<?> consumptionPeakHeatmap(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		return ResponseEntity.ok(consumptionMetrics.peakHeatmap(range[0], range[1]));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/security/summary")
	public ResponseEntity<?> securitySummary(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] range = resolve(from, to);
		if (range == null) {
			return rangeError();
		}
		return ResponseEntity.ok(securityMetrics.summary(range[0], range[1]));
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
