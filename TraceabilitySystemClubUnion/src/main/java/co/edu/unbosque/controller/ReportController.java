package co.edu.unbosque.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.service.PersonPartnerService;
import co.edu.unbosque.service.ReportService;

@RestController
@RequestMapping("/reports")
public class ReportController {

	private static final long MAX_RANGE_DAYS = 366;

	private final ReportService reports;
	private final PersonPartnerService partnerService;

	public ReportController(ReportService reports, PersonPartnerService partnerService) {
		this.reports = reports;
		this.partnerService = partnerService;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/consumptions")
	public ResponseEntity<?> consumptions(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
			@RequestParam(required = false) String environment) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		return pdf(reports.consumptionsPdf(from, to, environment), "consumos");
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/income-by-environment")
	public ResponseEntity<?> incomeByEnvironment(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		return pdf(reports.incomeByEnvironmentPdf(from, to), "ingresos-por-ambiente");
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/partner-statement")
	public ResponseEntity<?> partnerStatement(
			@RequestParam(required = false) String identification,
			@RequestParam(required = false) Long shareNumber,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		String resolved;
		if (identification != null && !identification.isBlank()) {
			resolved = identification;
		} else if (shareNumber != null) {
			List<PersonPartner> list = partnerService.getByShareNumber(shareNumber);
			if (list == null || list.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			resolved = list.get(0).getIdentification();
		} else {
			return ResponseEntity.badRequest().build();
		}
		byte[] bytes = reports.partnerStatementPdf(resolved, from, to);
		if (bytes == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return pdf(bytes, "estado-de-cuenta-" + resolved);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/security")
	public ResponseEntity<?> security(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
		if (invalid(from, to)) {
			return ResponseEntity.badRequest().build();
		}
		return pdf(reports.securityPdf(from, to), "seguridad");
	}

	private boolean invalid(LocalDateTime from, LocalDateTime to) {
		return from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS;
	}

	private ResponseEntity<byte[]> pdf(byte[] bytes, String name) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment()
				.filename(name + "-" + LocalDate.now() + ".pdf").build());
		return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
	}
}
