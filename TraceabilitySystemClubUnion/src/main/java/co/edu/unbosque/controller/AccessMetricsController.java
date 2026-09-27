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
/**
 * Endpoints de métricas de afluencia: visitas, ocupación por ambiente y series de asistencia.
 *
 * <p>Conviene precisar qué miden estos indicadores. Las visitas provienen de la tabla de accesos, cuyas filas las crea el
 * registro de un consumo y no un control de entrada, de modo que <strong>miden actividad de consumo, no presencia
 * física</strong>: un socio que entra al club y no consume nada es invisible aquí.
 *
 * <p>Comparte con los demás controladores de métricas la política de ventana por defecto de 30 días y tope de 366, con los
 * mismos métodos auxiliares duplicados.
 *
 * @see co.edu.unbosque.service.AccessMetricsService
 */
public class AccessMetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final AccessMetricsService metrics;

	public AccessMetricsController(AccessMetricsService metrics) {
		this.metrics = metrics;
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	/**
	 * Devuelve los indicadores de afluencia del periodo: visitas, socios distintos, frecuencia media y presentes.
	 *
	 * <p><strong>El número de presentes ignora el rango</strong> y se refiere siempre al momento actual, porque la consulta
	 * que lo resuelve no acepta fechas. En una consulta histórica ese valor es un dato en vivo mezclado con datos del
	 * pasado, y el cliente no tiene forma de distinguirlo.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to   fin del periodo; si se omite, ahora
	 * @return {@code 200} con los indicadores, o {@code 400} si el rango es inválido
	 */
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
	/**
	 * Devuelve el número de socios distintos que consumieron hoy en cada ambiente.
	 *
	 * <p><strong>No acepta parámetros:</strong> siempre resuelve el día en curso, de medianoche a medianoche.
	 *
	 * <p>Pese a estar en el controlador de accesos, la ocupación se deriva de los <strong>consumos</strong> y no de la tabla
	 * de accesos: es una medida de actividad por espacio, no de aforo. Además excluye los consumos sin socio asociado, a
	 * diferencia de las métricas de facturación, de modo que dos indicadores del mismo día pueden no cuadrar entre sí.
	 *
	 * @return {@code 200} con un elemento por ambiente
	 */
	@GetMapping("/occupancy")
	public ResponseEntity<?> occupancy() {
		return ResponseEntity.ok(metrics.occupancyToday());
	}

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	/**
	 * Devuelve la serie temporal de visitas con la granularidad indicada.
	 *
	 * <p>La serie no tiene huecos: los periodos sin visitas se devuelven en cero.
	 *
	 * @param from        inicio del periodo; si se omite, 30 días atrás
	 * @param to          fin del periodo; si se omite, ahora
	 * @param granularity {@code day}, {@code week} o {@code month}
	 * @return {@code 200} con la serie, o {@code 400} con un mensaje si el rango es inválido o la granularidad no se
	 *         reconoce
	 */
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

	/**
	 * Normaliza y valida la ventana temporal solicitada, con los mismos criterios que los demás controladores de métricas:
	 * fin igual a ahora, inicio 30 días antes, tope de 366 días.
	 *
	 * @param from inicio solicitado, o {@code null}
	 * @param to   fin solicitado, o {@code null}
	 * @return un arreglo con el inicio y el fin normalizados, o {@code null} si el rango no es aceptable
	 */
	private LocalDateTime[] resolve(LocalDateTime from, LocalDateTime to) {
		LocalDateTime effectiveTo = to != null ? to : LocalDateTime.now();
		LocalDateTime effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
		if (effectiveFrom.isAfter(effectiveTo)
				|| ChronoUnit.DAYS.between(effectiveFrom, effectiveTo) > MAX_RANGE_DAYS) {
			return null;
		}
		return new LocalDateTime[] { effectiveFrom, effectiveTo };
	}

	/**
	 * Construye la respuesta de rango inválido.
	 *
	 * @return {@code 400} con un cuerpo JSON que contiene el mensaje de error
	 */
	private ResponseEntity<?> rangeError() {
		return ResponseEntity.badRequest().body(Map.of("message", "El rango no puede superar 1 año"));
	}
}
