package co.edu.unbosque.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.ProductMetricsService;

@RestController
@RequestMapping("/metrics/products")
public class ProductMetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final ProductMetricsService metrics;
	private final PersonPartnerService partnerService;

	public ProductMetricsController(ProductMetricsService metrics, PersonPartnerService partnerService) {
		this.metrics = metrics;
		this.partnerService = partnerService;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/top")
	public ResponseEntity<?> top(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(required = false) String environment,
			@RequestParam(defaultValue = "revenue") String sort,
			@RequestParam(defaultValue = "20") int limit) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		return ResponseEntity.ok(metrics.top(r[0], r[1], environment, sort, limit));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/category-mix")
	public ResponseEntity<?> categoryMix(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		return ResponseEntity.ok(metrics.categoryMix(r[0], r[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/by-environment")
	public ResponseEntity<?> byEnvironment(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		return ResponseEntity.ok(metrics.byEnvironmentCategory(r[0], r[1]));
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/partner/{identification}")
	public ResponseEntity<?> partner(
			@PathVariable String identification,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "20") int limit) {
		return partnerTop(identification, from, to, limit);
	}

	@GetMapping("/partner/me")
	public ResponseEntity<?> me(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "20") int limit) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		return partnerTop(identification, from, to, limit);
	}

	private ResponseEntity<?> partnerTop(String identification, LocalDateTime from, LocalDateTime to, int limit) {
		LocalDateTime[] r = resolve(from, to);
		if (r == null) {
			return rangeError();
		}
		PersonPartner p = partnerService.getByIdentification(identification);
		if (p == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(metrics.topByPartner(p.getPersonId(), r[0], r[1], limit));
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
