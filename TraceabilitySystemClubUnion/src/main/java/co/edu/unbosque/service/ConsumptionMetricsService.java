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
public class ConsumptionMetricsService {

	private final PartnerConsumptionRepository repository;

	public ConsumptionMetricsService(PartnerConsumptionRepository repository) {
		this.repository = repository;
	}

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
