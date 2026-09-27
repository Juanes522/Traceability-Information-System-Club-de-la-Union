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
/**
 * Endpoints de métricas individuales de un socio.
 *
 * <p>Expone la misma información por dos vías con privilegios distintos: {@code /me}, que cualquier socio usa sobre sí
 * mismo, y {@code /{identification}}, reservado a {@code MANAGER} y {@code ADMIN}. Es el único subsistema de analítica cuyo
 * destinatario puede ser el propio socio.
 *
 * <p><strong>Nota sobre la resolución de rutas.</strong> La plantilla {@code /{identification}} se declara
 * <em>antes</em> que {@code /me}, de modo que una petición a {@code /metrics/partner/me} encaja formalmente con ambas. Lo
 * que hace que gane {@code /me} es que el emparejador de rutas de Spring prioriza los segmentos literales sobre las
 * plantillas; el orden de declaración no influye. La consecuencia curiosa es que un socio cuya identificación fuera
 * literalmente {@code "me"} no podría consultarse por la vía privilegiada.
 *
 * @see co.edu.unbosque.service.PartnerMetricsService
 */
public class PartnerMetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final PartnerMetricsService metrics;
	private final PersonPartnerService partnerService;

	public PartnerMetricsController(PartnerMetricsService metrics, PersonPartnerService partnerService) {
		this.metrics = metrics;
		this.partnerService = partnerService;
	}

	/**
	 * Devuelve las métricas del propio socio: su facturación, sus ambientes, su tendencia y sus visitas.
	 *
	 * <p>Patrón {@code /me}: la identidad se resuelve del contexto de seguridad y no se acepta ningún identificador, lo que
	 * hace el acceso seguro sin necesidad de guarda de rol.
	 *
	 * <p>Nótese que la fecha de última visita del resultado <strong>ignora el rango solicitado</strong>, a diferencia del
	 * resto de los campos.
	 *
	 * @param from        inicio del periodo; si se omite, 30 días atrás
	 * @param to          fin del periodo; si se omite, ahora
	 * @param granularity {@code day}, {@code week} o {@code month} para la serie temporal
	 * @return {@code 200} con las métricas propias; {@code 400} si el rango o la granularidad son inválidos; {@code 401} sin
	 *         sesión; {@code 404} si el socio no existe
	 */
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
	/**
	 * Devuelve las métricas de un socio indicado por su identificación.
	 *
	 * <p>Variante privilegiada de {@link #me}: acepta un identificador, y lo que impide el acceso indebido es el
	 * {@code @PreAuthorize} de rol.
	 *
	 * @param identification cédula del socio a consultar
	 * @param from           inicio del periodo; si se omite, 30 días atrás
	 * @param to             fin del periodo; si se omite, ahora
	 * @param granularity    {@code day}, {@code week} o {@code month}
	 * @return {@code 200} con las métricas; {@code 400} si el rango o la granularidad son inválidos; {@code 404} si el socio
	 *         no existe
	 */
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
