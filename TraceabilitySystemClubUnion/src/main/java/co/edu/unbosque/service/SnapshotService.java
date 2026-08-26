package co.edu.unbosque.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.AccessSummaryDTO;
import co.edu.unbosque.dto.ConsumptionSummaryDTO;
import co.edu.unbosque.dto.MonthlySnapshotDTO;
import co.edu.unbosque.model.MonthlySnapshot;
import co.edu.unbosque.repository.MonthlySnapshotRepository;
import co.edu.unbosque.repository.PartnerConsumptionRepository;

@Service
public class SnapshotService {

	private final ConsumptionMetricsService consumptionMetrics;
	private final AccessMetricsService accessMetrics;
	private final MonthlySnapshotRepository snapshotRepo;
	private final PartnerConsumptionRepository consumptionRepo;

	public SnapshotService(ConsumptionMetricsService consumptionMetrics, AccessMetricsService accessMetrics,
			MonthlySnapshotRepository snapshotRepo, PartnerConsumptionRepository consumptionRepo) {
		this.consumptionMetrics = consumptionMetrics;
		this.accessMetrics = accessMetrics;
		this.snapshotRepo = snapshotRepo;
		this.consumptionRepo = consumptionRepo;
	}

	public void snapshotMonth(YearMonth month) {
		LocalDateTime from = month.atDay(1).atStartOfDay();
		LocalDateTime to = month.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);
		ConsumptionSummaryDTO cs = consumptionMetrics.summary(from, to);
		AccessSummaryDTO as = accessMetrics.summary(from, to);
		String key = month.toString();
		MonthlySnapshot snap = snapshotRepo.findByYearMonth(key).orElseGet(MonthlySnapshot::new);
		snap.setYearMonth(key);
		snap.setTotalBilled(cs.getTotalBilled());
		snap.setTotalConsumption(cs.getTotalConsumption());
		snap.setTotalIva(cs.getTotalIva());
		snap.setTotalService(cs.getTotalService());
		snap.setTotalTip(cs.getTotalTip());
		snap.setChargeCount(cs.getChargeCount());
		snap.setAveragePerAccount(cs.getAveragePerAccount());
		snap.setTipPercentage(cs.getTipPercentage());
		snap.setVisits(as.getVisits());
		snap.setUniquePartners(as.getUniquePartners());
		snap.setGeneratedAt(LocalDateTime.now());
		snapshotRepo.save(snap);
	}

	@Scheduled(cron = "0 0 3 1 * *")
	public void snapshotPreviousMonth() {
		snapshotMonth(YearMonth.now().minusMonths(1));
	}

	public void backfillMissing() {
		LocalDateTime earliest = consumptionRepo.findEarliestConsumption();
		if (earliest == null) {
			return;
		}
		YearMonth m = YearMonth.from(earliest);
		YearMonth last = YearMonth.now().minusMonths(1);
		while (!m.isAfter(last)) {
			if (!snapshotRepo.existsByYearMonth(m.toString())) {
				snapshotMonth(m);
			}
			m = m.plusMonths(1);
		}
	}

	public List<MonthlySnapshotDTO> list() {
		List<MonthlySnapshotDTO> result = new ArrayList<>();
		for (MonthlySnapshot s : snapshotRepo.findAllByOrderByYearMonthAsc()) {
			MonthlySnapshotDTO dto = new MonthlySnapshotDTO();
			dto.setYearMonth(s.getYearMonth());
			dto.setTotalBilled(s.getTotalBilled());
			dto.setChargeCount(s.getChargeCount());
			dto.setAveragePerAccount(s.getAveragePerAccount());
			dto.setTipPercentage(s.getTipPercentage());
			dto.setVisits(s.getVisits());
			dto.setUniquePartners(s.getUniquePartners());
			result.add(dto);
		}
		return result;
	}
}
