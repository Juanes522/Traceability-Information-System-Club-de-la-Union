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
public class AccessMetricsService {

	private final AccessRepository accessRepo;
	private final PartnerConsumptionRepository consumptionRepo;

	public AccessMetricsService(AccessRepository accessRepo, PartnerConsumptionRepository consumptionRepo) {
		this.accessRepo = accessRepo;
		this.consumptionRepo = consumptionRepo;
	}

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
