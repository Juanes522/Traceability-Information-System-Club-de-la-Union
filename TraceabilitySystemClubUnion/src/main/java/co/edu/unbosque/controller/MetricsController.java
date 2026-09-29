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

/**
 * Endpoints de métricas de facturación y del panel de seguridad.
 *
 * <p>Es el controlador de analítica principal: sirve los indicadores, la distribución por ambiente, la tendencia, la
 * comparación intermensual y los mapas de concentración horaria que componen el tablero de gerencia.
 *
 * <p>Incluye además el resumen de seguridad, que rompe el patrón del resto: consulta la bitácora de auditoría en
 * Elasticsearch en lugar de la base relacional, y es el único endpoint del controlador reservado a {@code ADMIN}.
 *
 * <p><strong>Política de rango:</strong> ventana por defecto de los últimos 30 días y tope de 366. Esa política, junto con
 * los métodos auxiliares que la implementan, está <strong>duplicada</strong> en los controladores de accesos, socio,
 * productos y reportes; y difiere de la de 92 días que aplican los controladores de listados y auditoría.
 *
 * @see co.edu.unbosque.service.ConsumptionMetricsService
 * @see co.edu.unbosque.service.SecurityMetricsService
 */
@RestController
@RequestMapping("/metrics")
public class MetricsController {

	private static final long MAX_RANGE_DAYS = 366;

	private final ConsumptionMetricsService consumptionMetrics;
	private final SecurityMetricsService securityMetrics;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param consumptionMetrics el valor de consumption metrics
	 * @param securityMetrics el valor de security metrics
	 */
	public MetricsController(ConsumptionMetricsService consumptionMetrics, SecurityMetricsService securityMetrics) {
		this.consumptionMetrics = consumptionMetrics;
		this.securityMetrics = securityMetrics;
	}

	/**
	 * Devuelve los indicadores globales de facturación del periodo.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to fin del periodo; si se omite, ahora
	 * @return {@code 200} con los indicadores, o {@code 400} si el rango está invertido o excede 366 días
	 */
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

	/**
	 * Devuelve la facturación distribuida por ambiente, con su peso porcentual.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to fin del periodo; si se omite, ahora
	 * @return {@code 200} con un elemento por ambiente, ordenados de mayor a menor, o {@code 400} si el rango es inválido
	 */
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

	/**
	 * Devuelve la serie temporal de facturación con la granularidad indicada.
	 *
	 * <p>La serie no tiene huecos: los periodos sin actividad se devuelven en cero, de modo que el cliente puede dibujar
	 * una línea continua sin interpolar.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to fin del periodo; si se omite, ahora
	 * @param granularity {@code day}, {@code week} o {@code month}
	 * @return {@code 200} con la serie, o {@code 400} con un mensaje si el rango es inválido o la granularidad no se
	 *         reconoce
	 */
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

	/**
	 * Compara la facturación de un mes con la del mes anterior.
	 *
	 * <p>Es el único endpoint de métricas que <strong>no acepta un rango sino un mes</strong>, y por tanto el único
	 * independiente de la ventana temporal que el usuario haya elegido en el tablero.
	 *
	 * @param month mes en formato {@code YYYY-MM}; si se omite, el mes en curso
	 * @return {@code 200} con los totales de ambos periodos y la variación porcentual, o {@code 400} con un mensaje si el
	 *         formato del mes no es válido
	 */
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

	/**
	 * Devuelve la facturación agregada por hora del día y por día de la semana.
	 *
	 * <p>Ambas series se devuelven completas, con ceros en las franjas sin actividad.
	 *
	 * <p><strong>Ninguna pantalla lo consume:</strong> el tablero usa el mapa de calor, que cruza las dos dimensiones en
	 * lugar de agregarlas por separado.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to fin del periodo; si se omite, ahora
	 * @return {@code 200} con las dos series, o {@code 400} si el rango es inválido
	 */
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

	/**
	 * Devuelve la matriz día de la semana × hora de facturación, para el mapa de calor del tablero.
	 *
	 * <p>Emite <strong>solo las celdas con actividad</strong>, a diferencia del endpoint anterior: el cliente debe tratar
	 * las ausentes como cero al dibujar la matriz completa. El día de la semana se numera desde cero, con el lunes como
	 * cero.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to fin del periodo; si se omite, ahora
	 * @return {@code 200} con las celdas no vacías, o {@code 400} si el rango es inválido
	 */
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

	/**
	 * Devuelve los contadores del panel de seguridad: fallos de autenticación, bloqueos por tasa, accesos denegados y
	 * alertas críticas.
	 *
	 * <p>Consulta la bitácora en Elasticsearch, no la base relacional, y es el único endpoint de este controlador reservado
	 * a {@code ADMIN}.
	 *
	 * <p><strong>Nunca falla por indisponibilidad de la bitácora:</strong> si Elasticsearch no responde devuelve 200 con el
	 * indicador {@code degraded} activo y contadores parciales. El cliente debe mostrar ese indicador, porque unos
	 * contadores en cero podrían interpretarse como ausencia de incidentes cuando en realidad significan ausencia de datos.
	 *
	 * @param from inicio del periodo; si se omite, 30 días atrás
	 * @param to fin del periodo; si se omite, ahora
	 * @return {@code 200} con los contadores, posiblemente degradados, o {@code 400} si el rango es inválido
	 */
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

	/**
	 * Normaliza y valida la ventana temporal solicitada.
	 *
	 * <p>Aplica los valores por defecto —fin igual a ahora, inicio 30 días antes— y rechaza los rangos invertidos o
	 * superiores a 366 días. Ese tope acota el costo de las agregaciones, que no están paginadas.
	 *
	 * <p>Existe una copia casi idéntica de este método en cuatro controladores más.
	 *
	 * @param from inicio solicitado, o {@code null} para usar el valor por defecto
	 * @param to fin solicitado, o {@code null} para usar el valor por defecto
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
