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

import co.edu.unbosque.dto.AccessSummaryDTO;
import co.edu.unbosque.dto.AttendancePointDTO;
import co.edu.unbosque.dto.EnvironmentOccupancyDTO;
import co.edu.unbosque.model.Access;
import co.edu.unbosque.repository.AccessRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;

@Service
/**
 * Métricas de afluencia: visitas, socios distintos, presencia actual y ocupación por ambiente.
 *
 * <p>Conviene precisar qué miden realmente estos indicadores. Las visitas provienen de la tabla de accesos, cuyas filas
 * <strong>las crea el registro de un consumo</strong> y no un torniquete (véase {@link AccessService}). De modo que
 * toda métrica de este servicio mide <strong>actividad de consumo, no presencia física</strong>: un socio que entra al
 * club y no consume nada es invisible aquí.
 *
 * <p>Inyecta además el repositorio de consumos, lo que resulta llamativo en un servicio de accesos: lo necesita para
 * {@link #occupancyToday()}, que deriva la ocupación de los consumos y no de los accesos.
 *
 * <p>Comparte tres métodos privados de agrupación temporal, duplicados literalmente, con
 * {@link ConsumptionMetricsService} y {@link PartnerMetricsService}.
 */
public class AccessMetricsService {

	private final AccessRepository accessRepo;
	private final PartnerConsumptionRepository consumptionRepo;

	public AccessMetricsService(AccessRepository accessRepo, PartnerConsumptionRepository consumptionRepo) {
		this.accessRepo = accessRepo;
		this.consumptionRepo = consumptionRepo;
	}

	/**
	 * Calcula los indicadores de afluencia del rango.
	 *
	 * <p>Devuelve el número de visitas, el de socios distintos, la frecuencia media de visita por socio —visitas
	 * divididas entre socios distintos— y el número de socios presentes.
	 *
	 * <p><strong>Advertencia: el indicador de presentes ignora el rango solicitado.</strong> Cuenta siempre las visitas
	 * abiertas <em>en este momento</em>, porque la consulta que lo resuelve no acepta fechas. En una consulta histórica
	 * ese valor es un dato en vivo mezclado con datos del pasado, y el cliente no tiene forma de distinguirlo.
	 *
	 * @param from inicio del rango, inclusivo
	 * @param to   fin del rango, inclusivo
	 * @return los indicadores de afluencia
	 */
	public AccessSummaryDTO summary(LocalDateTime from, LocalDateTime to) {
		long visits = accessRepo.countByDateTimeAdmissionBetween(from, to);
		long unique = accessRepo.countDistinctPartnersInRange(from, to);
		AccessSummaryDTO dto = new AccessSummaryDTO();
		dto.setPresentNow(accessRepo.countByDateTimeDepartureIsNull());
		dto.setVisits(visits);
		dto.setUniquePartners(unique);
		dto.setAvgFrequency(unique > 0 ? (double) visits / unique : 0.0);
		return dto;
	}

	/**
	 * Cuenta los socios distintos que consumieron hoy en cada ambiente.
	 *
	 * <p>No acepta rango: siempre resuelve el día en curso, de medianoche a medianoche.
	 *
	 * <p><strong>Dos particularidades que conviene conocer.</strong> Primera: aunque vive en el servicio de accesos,
	 * <strong>deriva la ocupación de la tabla de consumos</strong>, no de la de accesos; es una medida de actividad por
	 * espacio, no de aforo. Segunda: la consulta que la resuelve referencia al socio del consumo, lo que genera una
	 * unión interna y <strong>excluye los consumos sin socio asociado</strong>, a diferencia de las métricas de
	 * facturación, que los incluyen. Dos indicadores del mismo día pueden por tanto no cuadrar entre sí.
	 *
	 * @return un elemento por ambiente con el número de socios distintos que consumieron hoy
	 */
	public List<EnvironmentOccupancyDTO> occupancyToday() {
		LocalDateTime start = LocalDate.now().atStartOfDay();
		LocalDateTime end = start.plusDays(1).minusNanos(1);
		List<Object[]> rows = consumptionRepo.occupancyByEnvironment(start, end);
		List<EnvironmentOccupancyDTO> result = new ArrayList<>();
		for (Object[] r : rows) {
			result.add(new EnvironmentOccupancyDTO((String) r[0], ((Number) r[1]).longValue()));
		}
		return result;
	}

	/**
	 * Calcula la serie temporal de visitas con la granularidad indicada.
	 *
	 * <p>Como la serie de facturación, <strong>pre-siembra todas las cubetas del rango</strong> para que no haya huecos:
	 * un día sin visitas aparece con cero.
	 *
	 * <p>Consideración de rendimiento: a diferencia de las métricas de facturación, que agregan en SQL, este método
	 * <strong>recupera todas las visitas del rango y las cuenta en Java</strong>, hidratando las entidades completas
	 * aunque solo necesite su fecha de entrada y sin imponer ningún tope. Un rango amplio sobre un padrón activo carga
	 * en memoria un volumen apreciable de filas.
	 *
	 * @param from        inicio del rango, inclusivo
	 * @param to          fin del rango, inclusivo
	 * @param granularity {@code day}, {@code week} o {@code month}
	 * @return la serie ordenada, con una entrada por cubeta del rango
	 * @throws IllegalArgumentException si la granularidad no es una de las tres admitidas
	 */
	public List<AttendancePointDTO> attendance(LocalDateTime from, LocalDateTime to, String granularity) {
		validateGranularity(granularity);
		Map<String, long[]> buckets = new LinkedHashMap<>();
		for (String key : bucketKeysInRange(from, to, granularity)) {
			buckets.put(key, new long[]{0L});
		}
		for (Access a : accessRepo.findByDateTimeAdmissionBetween(from, to)) {
			String key = bucketKey(a.getDateTimeAdmission(), granularity);
			buckets.computeIfAbsent(key, k -> new long[]{0L})[0] += 1;
		}
		List<AttendancePointDTO> result = new ArrayList<>();
		for (Map.Entry<String, long[]> e : buckets.entrySet()) {
			result.add(new AttendancePointDTO(e.getKey(), e.getValue()[0]));
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
				if (!k.equals(last)) { keys.add(k); last = k; }
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
			case "day": return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			case "week":
				WeekFields wf = WeekFields.ISO;
				return String.format("%d-W%02d", dt.get(wf.weekBasedYear()), dt.get(wf.weekOfWeekBasedYear()));
			case "month": return dt.format(DateTimeFormatter.ofPattern("yyyy-MM"));
			default: throw new IllegalArgumentException("Granularidad no soportada: " + granularity);
		}
	}

	private void validateGranularity(String granularity) {
		if (!"day".equals(granularity) && !"week".equals(granularity) && !"month".equals(granularity)) {
			throw new IllegalArgumentException("Granularidad no soportada: " + granularity);
		}
	}
}
