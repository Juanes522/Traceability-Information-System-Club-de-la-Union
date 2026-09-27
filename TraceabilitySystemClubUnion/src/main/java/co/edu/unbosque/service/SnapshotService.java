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
/**
 * Calcula y mantiene los resúmenes mensuales de facturación y afluencia.
 *
 * <p>Existe para que las series históricas no obliguen a reagregar todo el histórico de consumos en cada consulta:
 * un mes cerrado no cambia, de modo que su resumen se calcula una vez y se guarda.
 *
 * <p>Tiene dos disparadores y una sola implementación: un trabajo programado el día 1 de cada mes y un relleno de
 * meses faltantes que {@link co.edu.unbosque.config.SnapshotBackfillRunner} ejecuta en cada arranque.
 *
 * <p>La clase <strong>no es transaccional</strong>. Cada mes se guarda por separado, así que un fallo a mitad de un
 * relleno largo conserva los meses ya calculados.
 *
 * @see co.edu.unbosque.model.MonthlySnapshot
 * @see co.edu.unbosque.config.SnapshotBackfillRunner
 */
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

	/**
	 * Calcula y guarda el resumen de un mes, creándolo o actualizándolo.
	 *
	 * <p>Combina las métricas de consumo y las de acceso del mes en una sola fila. Es un <em>upsert</em> por
	 * {@code yearMonth}, de modo que recalcular un mes ya existente sobrescribe sus valores.
	 *
	 * <p><strong>Trata la frontera del mes correctamente</strong>, con un rango semiabierto:
	 *
	 * <pre>
	 * from = mes.atDay(1).atStartOfDay();
	 * to   = mes.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);
	 * </pre>
	 *
	 * <p>Ese {@code minusNanos(1)} importa: las consultas de agregación usan {@code BETWEEN}, que es inclusivo en
	 * ambos extremos, así que sin él un consumo registrado exactamente a medianoche del día 1 se contaría en dos
	 * meses. Conviene saber que {@link ConsumptionMetricsService#comparison(YearMonth)} resuelve esa misma frontera
	 * <strong>sin</strong> esta corrección, y que por eso las dos respuestas para «el total de este mes» pueden
	 * discrepar.
	 *
	 * <p>El indicador de «socios presentes ahora» de las métricas de acceso se descarta a propósito: es un dato
	 * instantáneo y carece de sentido en un histórico.
	 *
	 * @param month mes a resumir
	 */
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

	/**
	 * Resume el mes anterior, el día 1 de cada mes a las 03:00.
	 *
	 * <p>Se ejecuta sobre el mes <strong>ya cerrado</strong>, no sobre el corriente: un resumen del mes en curso
	 * estaría incompleto y, por la lógica de {@link #backfillMissing()}, nunca se corregiría.
	 */
	@Scheduled(cron = "0 0 3 1 * *")
	public void snapshotPreviousMonth() {
		snapshotMonth(YearMonth.now().minusMonths(1));
	}

	/**
	 * Calcula los resúmenes de todos los meses que falten, desde el primer consumo hasta el mes anterior al actual.
	 *
	 * <p>Cubre los huecos que el trabajo programado no puede llenar: el histórico previo a la puesta en marcha del
	 * sistema, y los meses perdidos si la aplicación estuvo detenida un día 1.
	 *
	 * <p>Es idempotente porque <strong>omite los meses que ya tienen resumen</strong>. Esa misma propiedad tiene una
	 * cara negativa: un mes capturado prematuramente, con datos incompletos, <strong>nunca se corrige</strong>;
	 * recalcularlo exige borrar la fila o llamar a {@link #snapshotMonth(YearMonth)} explícitamente.
	 *
	 * <p>Sobre una base sin consumos retorna de inmediato, ya que la consulta del consumo más antiguo devuelve nulo.
	 *
	 * <p>Lo invoca {@link co.edu.unbosque.config.SnapshotBackfillRunner} en <strong>cada arranque</strong>, sin
	 * guarda de perfil ni de configuración.
	 */
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

	/**
	 * Devuelve la serie histórica completa, en orden cronológico.
	 *
	 * <p><strong>La proyección es parcial:</strong> descarta el desglose monetario ({@code totalConsumption},
	 * {@code totalIva}, {@code totalService}, {@code totalTip}) y la fecha de generación. Esos campos se persisten
	 * pero ningún cliente los consulta.
	 *
	 * @return todos los resúmenes mensuales como DTO, del mes más antiguo al más reciente
	 */
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
