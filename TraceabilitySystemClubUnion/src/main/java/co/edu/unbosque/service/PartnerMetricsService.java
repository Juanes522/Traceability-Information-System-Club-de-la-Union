package co.edu.unbosque.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.ConsumptionSummaryDTO;
import co.edu.unbosque.dto.EnvironmentTotalDTO;
import co.edu.unbosque.dto.PartnerMetricsDTO;
import co.edu.unbosque.dto.TrendPointDTO;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.repository.AccessRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;

/**
 * Métricas individuales de un socio: su consumo, sus ambientes, su tendencia y sus visitas.
 *
 * <p>Sirve tanto el panel del propio socio ({@code /metrics/partner/me}) como su consulta por parte de un gestor. Es el
 * único servicio de analítica cuyo destinatario puede ser el socio mismo.
 *
 * <p><strong>Se aparta de la estrategia del resto de las métricas.</strong> Mientras
 * {@link ConsumptionMetricsService} agrega en SQL, este servicio <strong>hidrata todas las entidades del rango y
 * agrega en memoria</strong>. Funciona porque el volumen de un socio individual es mucho menor que el global, pero la
 * inconsistencia es real: no usa proyección, no impone tope, y reimplementa en Java las tres agregaciones que su
 * equivalente global resuelve en la base.
 *
 * <p>Comparte con los otros dos servicios de métricas tres métodos privados de agrupación temporal, duplicados
 * literalmente, y una cuarta copia de la fórmula del total del consumo.
 *
 * @see co.edu.unbosque.dto.PartnerMetricsDTO
 */
@Service
public class PartnerMetricsService {

	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final PartnerConsumptionRepository repository;
	private final AccessRepository accessRepo;

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param repository el valor de repository
	 * @param accessRepo el valor de access repo
	 */
	public PartnerMetricsService(PartnerConsumptionRepository repository, AccessRepository accessRepo) {
		this.repository = repository;
		this.accessRepo = accessRepo;
	}

	/**
	 * Calcula el conjunto completo de métricas de un socio en un rango.
	 *
	 * <p>Compone cuatro bloques: el resumen de su facturación, su distribución por ambiente, su serie temporal y sus
	 * visitas.
	 *
	 * <p>Los tres primeros se calculan <strong>en memoria</strong> sobre las entidades del rango; los ambientes se
	 * ordenan de mayor a menor consumo y los consumos sin ambiente se agrupan bajo una etiqueta genérica.
	 *
	 * <p><strong>Advertencia sobre la última visita.</strong> A diferencia de todos los demás campos del DTO, la fecha
	 * de última visita <strong>ignora el rango solicitado</strong>: es la última visita registrada del socio, que puede
	 * ser muy anterior a la ventana consultada. Cuando el socio nunca ha visitado el club se informa como cadena vacía,
	 * no como nulo.
	 *
	 * @param personId clave primaria del socio
	 * @param from inicio del rango, inclusivo
	 * @param to fin del rango, inclusivo
	 * @param granularity {@code day}, {@code week} o {@code month} para la serie temporal
	 * @return las métricas del socio; con valores en cero si no tuvo actividad en el rango
	 * @throws IllegalArgumentException si la granularidad no es una de las tres admitidas
	 */
	public PartnerMetricsDTO forPartner(Long personId, LocalDateTime from, LocalDateTime to, String granularity) {
		validateGranularity(granularity);
		List<PartnerConsumption> rows = repository.findByPartnerPersonIdAndConsumptionOpeningBetween(personId, from, to);
		PartnerMetricsDTO dto = new PartnerMetricsDTO(summary(rows), byEnvironment(rows), trend(rows, from, to, granularity));
		dto.setVisits(accessRepo.countByPartnerPersonIdAndDateTimeAdmissionBetween(personId, from, to));
		dto.setLastVisit(accessRepo.findFirstByPartnerPersonIdOrderByDateTimeAdmissionDesc(personId)
				.map(a -> a.getDateTimeAdmission().format(DTF)).orElse(""));
		return dto;
	}

	private ConsumptionSummaryDTO summary(List<PartnerConsumption> rows) {
		double value = 0.0, iva = 0.0, service = 0.0, tip = 0.0;
		for (PartnerConsumption c : rows) {
			value += safe(c.getConsumptionValue());
			iva += safe(c.getIva());
			service += safe(c.getService());
			tip += safe(c.getTip());
		}
		long count = rows.size();
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

	private List<EnvironmentTotalDTO> byEnvironment(List<PartnerConsumption> rows) {
		Map<String, double[]> map = new LinkedHashMap<>();
		double global = 0.0;
		for (PartnerConsumption c : rows) {
			String env = c.getEnviroment() == null ? "—" : c.getEnviroment();
			double t = total(c);
			global += t;
			double[] agg = map.computeIfAbsent(env, k -> new double[]{0.0, 0.0});
			agg[0] += t;
			agg[1] += 1;
		}
		List<EnvironmentTotalDTO> result = new ArrayList<>();
		for (Map.Entry<String, double[]> e : map.entrySet()) {
			double t = e.getValue()[0];
			result.add(new EnvironmentTotalDTO(e.getKey(), t, (long) e.getValue()[1], global > 0 ? t / global * 100.0 : 0.0));
		}
		result.sort((a, b) -> Double.compare(b.getTotal(), a.getTotal()));
		return result;
	}

	private List<TrendPointDTO> trend(List<PartnerConsumption> rows, LocalDateTime from, LocalDateTime to, String granularity) {
		Map<String, double[]> buckets = new LinkedHashMap<>();
		for (String key : bucketKeysInRange(from, to, granularity)) {
			buckets.put(key, new double[]{0.0, 0.0});
		}
		for (PartnerConsumption c : rows) {
			String key = bucketKey(c.getConsumptionOpening(), granularity);
			double[] agg = buckets.computeIfAbsent(key, k -> new double[]{0.0, 0.0});
			agg[0] += total(c);
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
			LocalDate d = from.toLocalDate();
			LocalDate end = to.toLocalDate();
			while (!d.isAfter(end)) {
				keys.add(bucketKey(d.atStartOfDay(), "day"));
				d = d.plusDays(1);
			}
		} else if ("week".equals(granularity)) {
			LocalDate d = from.toLocalDate();
			LocalDate end = to.toLocalDate();
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

	private void validateGranularity(String granularity) {
		if (!"day".equals(granularity) && !"week".equals(granularity) && !"month".equals(granularity)) {
			throw new IllegalArgumentException("Granularidad no soportada: " + granularity);
		}
	}

	private double total(PartnerConsumption c) {
		return safe(c.getConsumptionValue()) + safe(c.getIva()) + safe(c.getService()) + safe(c.getTip());
	}

	private double safe(Double d) {
		return d == null ? 0.0 : d;
	}
}
