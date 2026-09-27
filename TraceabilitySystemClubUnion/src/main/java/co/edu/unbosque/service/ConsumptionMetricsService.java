package co.edu.unbosque.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.ComparisonDTO;
import co.edu.unbosque.dto.ConsumptionRowView;
import co.edu.unbosque.dto.ConsumptionSummaryDTO;
import co.edu.unbosque.dto.EnvironmentTotalDTO;
import co.edu.unbosque.dto.HourBucketDTO;
import co.edu.unbosque.dto.PeakDTO;
import co.edu.unbosque.dto.PeakHeatmapCellDTO;
import co.edu.unbosque.dto.TrendPointDTO;
import co.edu.unbosque.dto.WeekdayBucketDTO;
import co.edu.unbosque.repository.PartnerConsumptionRepository;

@Service
/**
 * Métricas de facturación: el servicio de analítica principal del sistema.
 *
 * <p>Sirve los tableros de gerencia y administración, y alimenta además los resúmenes mensuales de
 * {@link SnapshotService} y varios reportes PDF.
 *
 * <p><strong>Estrategia: agregar en SQL, derivar en Java.</strong> Las sumas y agrupaciones las resuelve la base de
 * datos; en Java solo se desempaquetan los resultados posicionales y se calculan los valores derivados —porcentajes,
 * promedios, variaciones—. Es la diferencia con {@link PartnerMetricsService}, que hidrata entidades y suma en
 * memoria.
 *
 * <p>Para las series temporales usa la proyección {@link co.edu.unbosque.dto.ConsumptionRowView}, que recupera solo
 * fecha e importes sin materializar entidades.
 *
 * <p>Tres métodos privados —la validación de granularidad y la generación de claves de agrupación— están
 * <strong>duplicados literalmente</strong> en {@link AccessMetricsService} y {@link PartnerMetricsService}.
 *
 * <p>Ninguno de estos métodos es transaccional ni lo necesita: son solo lectura.
 */
public class ConsumptionMetricsService {

	private final PartnerConsumptionRepository repository;

	public ConsumptionMetricsService(PartnerConsumptionRepository repository) {
		this.repository = repository;
	}

	/**
	 * Calcula los indicadores globales de facturación del rango.
	 *
	 * <p>Devuelve el desglose (consumo neto, IVA, servicio, propina), el total facturado, el número de cargos y dos
	 * valores derivados:
	 *
	 * <ul>
	 *   <li><strong>Valor promedio por cuenta</strong> = total facturado / número de cargos.</li>
	 *   <li><strong>Porcentaje de propina</strong> = propina / <strong>consumo neto</strong> × 100. Nótese que el
	 *       denominador es el consumo neto y <strong>no</strong> el total facturado: la propina se expresa como
	 *       porcentaje de lo consumido, que es la convención del negocio, no de lo cobrado.</li>
	 * </ul>
	 *
	 * <p>Ambos valores derivados devuelven cero cuando su denominador es cero, en lugar de propagar una división
	 * inválida.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to   fin del rango, inclusivo
	 * @return los indicadores del periodo; todos en cero si no hubo actividad
	 */
	public ConsumptionSummaryDTO summary(LocalDateTime from, LocalDateTime to) {
		Object[] row = repository.aggregateSummary(from, to).get(0);
		double value = num(row[0]);
		double iva = num(row[1]);
		double service = num(row[2]);
		double tip = num(row[3]);
		long count = ((Number) row[4]).longValue();
		double totalBilled = value + iva + service + tip;

		ConsumptionSummaryDTO dto = new ConsumptionSummaryDTO();
		dto.setTotalConsumption(value);
		dto.setTotalIva(iva);
		dto.setTotalService(service);
		dto.setTotalTip(tip);
		dto.setTotalBilled(totalBilled);
		dto.setChargeCount(count);
		dto.setAveragePerAccount(count > 0 ? totalBilled / count : 0.0);
		dto.setTipPercentage(value > 0 ? tip / value * 100.0 : 0.0);
		return dto;
	}

	/**
	 * Distribuye la facturación del rango por ambiente, con su peso porcentual.
	 *
	 * <p>Requiere <strong>dos pasadas</strong> sobre el resultado de la consulta: la primera obtiene el total global, y
	 * la segunda calcula el porcentaje de cada ambiente sobre él. La base no puede hacerlo en una sola agregación.
	 *
	 * <p>Los ambientes vienen ya ordenados de mayor a menor facturación desde SQL.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to   fin del rango, inclusivo
	 * @return un elemento por ambiente con su total, número de cargos y porcentaje
	 */
	public List<EnvironmentTotalDTO> byEnvironment(LocalDateTime from, LocalDateTime to) {
		List<Object[]> rows = repository.aggregateByEnvironment(from, to);
		double global = 0.0;
		for (Object[] r : rows) {
			global += num(r[1]);
		}
		List<EnvironmentTotalDTO> result = new ArrayList<>();
		for (Object[] r : rows) {
			double total = num(r[1]);
			double percentage = global > 0 ? total / global * 100.0 : 0.0;
			result.add(new EnvironmentTotalDTO((String) r[0], total, ((Number) r[2]).longValue(), percentage));
		}
		return result;
	}

	private double num(Object o) {
		return o == null ? 0.0 : ((Number) o).doubleValue();
	}

	/**
	 * Calcula la serie temporal de facturación con la granularidad indicada.
	 *
	 * <p><strong>Pre-siembra todas las claves del rango</strong> antes de acumular, de modo que la serie
	 * <strong>no tiene huecos</strong>: un día sin actividad aparece con valor cero en lugar de faltar. Eso es lo que
	 * permite al cliente dibujar una línea continua sin interpolar.
	 *
	 * <p>Recorre la proyección de filas en lugar de hidratar entidades, y acumula total y número de cargos por cubeta.
	 *
	 * @param from        inicio del rango, inclusivo
	 * @param to          fin del rango, inclusivo
	 * @param granularity {@code day}, {@code week} o {@code month}
	 * @return la serie ordenada, con una entrada por cubeta del rango
	 * @throws IllegalArgumentException si la granularidad no es una de las tres admitidas. El controlador la traduce a
	 *                                  una respuesta 400 con mensaje
	 */
	public List<TrendPointDTO> trend(LocalDateTime from, LocalDateTime to, String granularity) {
		bucketKeyValidate(granularity);
		Map<String, double[]> buckets = new LinkedHashMap<>();
		for (String key : bucketKeysInRange(from, to, granularity)) {
			buckets.put(key, new double[]{0.0, 0.0});
		}
		for (ConsumptionRowView r : repository.findRowsInRange(from, to)) {
			String key = bucketKey(r.getConsumptionOpening(), granularity);
			double[] agg = buckets.get(key);
			if (agg == null) {
				agg = new double[]{0.0, 0.0};
				buckets.put(key, agg);
			}
			agg[0] += rowTotal(r);
			agg[1] += 1;
		}
		List<TrendPointDTO> result = new ArrayList<>();
		for (Map.Entry<String, double[]> e : buckets.entrySet()) {
			result.add(new TrendPointDTO(e.getKey(), e.getValue()[0], (long) e.getValue()[1]));
		}
		return result;
	}

	private List<String> bucketKeysInRange(LocalDateTime from, LocalDateTime to, String granularity) {
		List<String> keys = new ArrayList<>();
		if ("day".equals(granularity)) {
			java.time.LocalDate d = from.toLocalDate();
			java.time.LocalDate end = to.toLocalDate();
			while (!d.isAfter(end)) {
				keys.add(bucketKey(d.atStartOfDay(), "day"));
				d = d.plusDays(1);
			}
		} else if ("week".equals(granularity)) {
			java.time.LocalDate d = from.toLocalDate();
			java.time.LocalDate end = to.toLocalDate();
			String last = null;
			while (!d.isAfter(end)) {
				String k = bucketKey(d.atStartOfDay(), "week");
				if (!k.equals(last)) {
					keys.add(k);
					last = k;
				}
				d = d.plusDays(1);
			}
		} else {
			YearMonth m = YearMonth.from(from);
			YearMonth end = YearMonth.from(to);
			while (!m.isAfter(end)) {
				keys.add(bucketKey(m.atDay(1).atStartOfDay(), "month"));
				m = m.plusMonths(1);
			}
		}
		return keys;
	}

	/**
	 * Calcula la facturación agregada por hora del día y por día de la semana.
	 *
	 * <p>Responde a la pregunta de cuándo se concentra la actividad, a efectos de dotación de personal.
	 *
	 * <p><strong>Emite siempre las 24 horas y los 7 días</strong>, rellenando con ceros las cubetas sin actividad. Los
	 * días se generan a partir del orden natural de la semana, de lunes a domingo.
	 *
	 * <p>Nótese la incoherencia de contrato con {@link #peakHeatmap(LocalDateTime, LocalDateTime)}, que resuelve el
	 * mismo concepto emitiendo <strong>solo las celdas no vacías</strong>. El cliente debe absorber esa diferencia.
	 *
	 * <p>Ninguna pantalla consume este método: el tablero usa el mapa de calor. Se conserva expuesto en
	 * {@code GET /metrics/consumption/peak}.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to   fin del rango, inclusivo
	 * @return las dos series, por hora y por día de la semana, completas
	 */
	public PeakDTO peak(LocalDateTime from, LocalDateTime to) {
		double[][] hours = new double[24][2];
		Map<DayOfWeek, double[]> days = new LinkedHashMap<>();
		for (DayOfWeek d : DayOfWeek.values()) {
			days.put(d, new double[]{0.0, 0.0});
		}
		for (ConsumptionRowView r : repository.findRowsInRange(from, to)) {
			LocalDateTime opening = r.getConsumptionOpening();
			double total = rowTotal(r);
			int h = opening.getHour();
			hours[h][0] += total;
			hours[h][1] += 1;
			double[] day = days.get(opening.getDayOfWeek());
			day[0] += total;
			day[1] += 1;
		}
		List<HourBucketDTO> byHour = new ArrayList<>();
		for (int h = 0; h < 24; h++) {
			byHour.add(new HourBucketDTO(h, hours[h][0], (long) hours[h][1]));
		}
		List<WeekdayBucketDTO> byWeekday = new ArrayList<>();
		for (DayOfWeek d : DayOfWeek.values()) {
			double[] agg = days.get(d);
			byWeekday.add(new WeekdayBucketDTO(d.name(), agg[0], (long) agg[1]));
		}
		return new PeakDTO(byHour, byWeekday);
	}

	/**
	 * Calcula la matriz día de la semana × hora de facturación, para el mapa de calor del tablero.
	 *
	 * <p>Es la versión bidimensional de {@link #peak(LocalDateTime, LocalDateTime)}: cruza las dos dimensiones en lugar
	 * de agregarlas por separado, lo que permite ver que el pico del viernes no está a la misma hora que el del martes.
	 *
	 * <p>El día de la semana se emite en base cero, con el lunes como cero.
	 *
	 * <p><strong>Emite únicamente las celdas con actividad</strong>, a diferencia de
	 * {@link #peak(LocalDateTime, LocalDateTime)}: el cliente debe tratar las celdas ausentes como cero al dibujar la
	 * matriz completa.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to   fin del rango, inclusivo
	 * @return las celdas con actividad, cada una con su día, hora, total y número de cargos
	 */
	public List<PeakHeatmapCellDTO> peakHeatmap(LocalDateTime from, LocalDateTime to) {
		Map<Integer, double[]> cells = new LinkedHashMap<>();
		for (ConsumptionRowView r : repository.findRowsInRange(from, to)) {
			LocalDateTime opening = r.getConsumptionOpening();
			int weekday = opening.getDayOfWeek().getValue() - 1;
			int hour = opening.getHour();
			int key = weekday * 24 + hour;
			double[] agg = cells.computeIfAbsent(key, k -> new double[]{0.0, 0.0});
			agg[0] += rowTotal(r);
			agg[1] += 1;
		}
		List<PeakHeatmapCellDTO> result = new ArrayList<>();
		for (Map.Entry<Integer, double[]> e : cells.entrySet()) {
			int weekday = e.getKey() / 24;
			int hour = e.getKey() % 24;
			result.add(new PeakHeatmapCellDTO(weekday, hour, e.getValue()[0], (long) e.getValue()[1]));
		}
		return result;
	}

	/**
	 * Compara la facturación de un mes con la del mes anterior.
	 *
	 * <p>Produce el indicador de variación porcentual del tablero. Cuando el mes previo no tuvo facturación la variación
	 * se informa como cero, en lugar de como un crecimiento infinito.
	 *
	 * <p><strong>Advertencia sobre las fronteras del mes.</strong> Los rangos se construyen así:
	 *
	 * <pre>
	 * curTo   = mes.plusMonths(1).atDay(1).atStartOfDay();
	 * prevTo  = curFrom;
	 * </pre>
	 *
	 * <p>Como la consulta de agregación usa {@code BETWEEN}, inclusivo en ambos extremos, y {@code prevTo} coincide
	 * exactamente con {@code curFrom}, <strong>un consumo registrado a medianoche del día 1 se cuenta en los dos
	 * periodos</strong>, y volverá a contarse en la comparación del mes siguiente.
	 *
	 * <p>{@link SnapshotService#snapshotMonth(YearMonth)} resuelve esa misma frontera correctamente, restando un
	 * nanosegundo al extremo superior. Las dos respuestas para «el total facturado de este mes» pueden por tanto
	 * discrepar, y la de este método es la que no corrige la frontera.
	 *
	 * @param month mes a comparar con su anterior
	 * @return los totales y recuentos de ambos periodos, y la variación porcentual
	 */
	public ComparisonDTO comparison(YearMonth month) {
		LocalDateTime curFrom = month.atDay(1).atStartOfDay();
		LocalDateTime curTo = month.plusMonths(1).atDay(1).atStartOfDay();
		LocalDateTime prevFrom = month.minusMonths(1).atDay(1).atStartOfDay();
		LocalDateTime prevTo = curFrom;

		Object[] cur = repository.aggregateSummary(curFrom, curTo).get(0);
		Object[] prev = repository.aggregateSummary(prevFrom, prevTo).get(0);
		double currentTotal = totalOf(cur);
		double previousTotal = totalOf(prev);

		ComparisonDTO dto = new ComparisonDTO();
		dto.setCurrentTotal(currentTotal);
		dto.setPreviousTotal(previousTotal);
		dto.setCurrentCount(((Number) cur[4]).longValue());
		dto.setPreviousCount(((Number) prev[4]).longValue());
		dto.setVariancePercentage(previousTotal > 0 ? (currentTotal - previousTotal) / previousTotal * 100.0 : 0.0);
		return dto;
	}

	private double rowTotal(ConsumptionRowView r) {
		return safe(r.getConsumptionValue()) + safe(r.getIva()) + safe(r.getService()) + safe(r.getTip());
	}

	private double safe(Double d) {
		return d == null ? 0.0 : d;
	}

	private double totalOf(Object[] row) {
		return num(row[0]) + num(row[1]) + num(row[2]) + num(row[3]);
	}

	private void bucketKeyValidate(String granularity) {
		if (!"day".equals(granularity) && !"week".equals(granularity) && !"month".equals(granularity)) {
			throw new IllegalArgumentException("Granularidad no soportada: " + granularity);
		}
	}

	private String bucketKey(LocalDateTime dt, String granularity) {
		switch (granularity) {
			case "day":
				return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			case "week":
				WeekFields wf = WeekFields.ISO;
				return String.format("%d-W%02d", dt.get(wf.weekBasedYear()), dt.get(wf.weekOfWeekBasedYear()));
			case "month":
				return dt.format(DateTimeFormatter.ofPattern("yyyy-MM"));
			default:
				throw new IllegalArgumentException("Granularidad no soportada: " + granularity);
		}
	}
}
