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
import co.edu.unbosque.service.PartnerMetricsService;
import co.edu.unbosque.service.PersonPartnerService;

@RestController
@RequestMapping("/metrics/partner")
public class PartnerMetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final PartnerMetricsService metrics;
	private final PersonPartnerService partnerService;

	public PartnerMetricsController(PartnerMetricsService metrics, PersonPartnerService partnerService) {
		this.metrics = metrics;
		this.partnerService = partnerService;
	}

	@GetMapping("/me")
	public ResponseEntity<?> me(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "day") String granularity) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String identification = ((UserDetails) auth.getPrincipal()).getUsername();
		return build(identification, from, to, granularity);
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/{identification}")
	public ResponseEntity<?> byIdentification(
			@PathVariable String identification,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(defaultValue = "day") String granularity) {
		return build(identification, from, to, granularity);
	}

	private ResponseEntity<?> build(String identification, LocalDateTime from, LocalDateTime to, String granularity) {
		LocalDateTime effectiveTo = to != null ? to : LocalDateTime.now();
		LocalDateTime effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
		if (effectiveFrom.isAfter(effectiveTo)
				|| ChronoUnit.DAYS.between(effectiveFrom, effectiveTo) > MAX_RANGE_DAYS) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 1 año"));
		}
		PersonPartner p = partnerService.getByIdentification(identification);
		if (p == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			return ResponseEntity.ok(metrics.forPartner(p.getPersonId(), effectiveFrom, effectiveTo, granularity));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}
}
