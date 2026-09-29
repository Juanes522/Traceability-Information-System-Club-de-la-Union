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

/**
 * Endpoint de consulta de la bitácora de auditoría.
 *
 * <p>Un solo método, reservado a {@code ADMIN}. Consulta el índice de Elasticsearch, no la base relacional.
 *
 * <p>Nótese que <strong>no hay DTO de salida</strong>: se serializa {@link co.edu.unbosque.model.AuditEvent} directamente,
 * de modo que la forma de ese documento es contrato público de la API.
 *
 * <p>La política de rango es la de 92 días que comparte con los controladores de listados, no la de 366 de las métricas.
 *
 * @see co.edu.unbosque.service.AuditQueryService
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

	private final AuditQueryService queryService;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param queryService el valor de query service
	 */
	public AuditController(AuditQueryService queryService) {
		this.queryService = queryService;
	}

	/**
	 * Busca eventos de auditoría con filtros opcionales, paginados y del más reciente al más antiguo.
	 *
	 * <p>Los filtros se combinan de forma conjuntiva y los vacíos se omiten, de modo que <strong>una petición sin filtros
	 * devuelve toda la bitácora</strong> paginada. Es lo que espera la pantalla de auditoría en su estado inicial.
	 *
	 * <p><strong>Particularidad del formato de fechas.</strong> Este es el único endpoint del sistema cuyos parámetros
	 * temporales son instantes absolutos y esperan por tanto una marca ISO-8601 <strong>con zona horaria</strong>; el resto
	 * de la API recibe fechas locales del servidor. El cliente debe convertir a UTC solo aquí, y de hecho su pantalla de
	 * auditoría es la única que lo hace.
	 *
	 * <p>La anotación de formato que acompaña a esos parámetros resulta engañosa: no es la que rige la conversión de un
	 * instante, que resuelve el convertidor por defecto del framework.
	 *
	 * @param username sujeto a filtrar, o vacío para no filtrar
	 * @param eventType tipo de evento, o vacío
	 * @param result desenlace, o vacío
	 * @param from inicio del rango, opcional
	 * @param to fin del rango, opcional
	 * @param page índice de página, base cero
	 * @param size tamaño de página
	 * @return {@code 200} con la página de eventos, o {@code 400} con un mensaje si el rango excede tres meses
	 */
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
