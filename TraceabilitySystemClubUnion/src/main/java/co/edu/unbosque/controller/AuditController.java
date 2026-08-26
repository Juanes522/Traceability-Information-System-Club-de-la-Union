package co.edu.unbosque.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.model.AuditEvent;
import co.edu.unbosque.service.AuditQueryService;

@RestController
@RequestMapping("/audit")
public class AuditController {

	private final AuditQueryService queryService;

	public AuditController(AuditQueryService queryService) {
		this.queryService = queryService;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<?> search(
			@RequestParam(required = false) String username,
			@RequestParam(required = false) String eventType,
			@RequestParam(required = false) String result,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92) {
			return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 3 meses"));
		}
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "timestamp"));
		return ResponseEntity.ok(queryService.search(username, eventType, result, from, to, pageable));
	}
}
