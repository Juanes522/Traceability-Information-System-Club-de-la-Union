package co.edu.unbosque.service;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import co.edu.unbosque.dto.CategoryMixDTO;
import co.edu.unbosque.dto.EnvironmentCategoryDTO;
import co.edu.unbosque.dto.EnvironmentTotalDTO;
import co.edu.unbosque.dto.ProductDetailDTO;
import co.edu.unbosque.dto.ProductRankDTO;
import co.edu.unbosque.dto.UserFailedCountDTO;
import co.edu.unbosque.model.AuditEvent;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PartnerConsumptionRepository;

@Service
public class ReportService {

	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private final NumberFormat usd = NumberFormat.getCurrencyInstance(Locale.US);

	private final PartnerConsumptionRepository consumptionRepo;
	private final PersonPartnerService partnerService;
	private final ConsumptionMetricsService consumptionMetrics;
	private final AuditQueryService auditQuery;
	private final ProductMetricsService productMetrics;

	public ReportService(PartnerConsumptionRepository consumptionRepo, PersonPartnerService partnerService,
			ConsumptionMetricsService consumptionMetrics, AuditQueryService auditQuery,
			ProductMetricsService productMetrics) {
		this.consumptionRepo = consumptionRepo;
		this.partnerService = partnerService;
		this.consumptionMetrics = consumptionMetrics;
		this.auditQuery = auditQuery;
		this.productMetrics = productMetrics;
		this.usd.setMaximumFractionDigits(2);
	}

	public byte[] consumptionsPdf(LocalDateTime from, LocalDateTime to, String environment) {
		List<PartnerConsumption> rows = (environment != null && !environment.isBlank())
				? consumptionRepo.findByEnviromentAndConsumptionOpeningBetween(environment, from, to)
				: consumptionRepo.findByConsumptionOpeningBetween(from, to);
		PdfSupport doc = new PdfSupport("Consumos por periodo", from, to);
		double total = 0.0;
		for (PartnerConsumption c : rows) {
			total += total(c);
		}
		int count = rows.size();
		doc.kpis(List.of(
				new String[]{"Total facturado", money(total)},
				new String[]{"N° de cargos", String.valueOf(count)},
				new String[]{"Valor promedio", money(count > 0 ? total / count : 0.0)}));
		if (!rows.isEmpty()) {
			Map<String, Double> byEnv = sumByEnvironment(rows);
			doc.barChart("Ingresos por ambiente", new ArrayList<>(byEnv.keySet()), new ArrayList<>(byEnv.values()));
			Map<String, Double> byDay = sumByDay(rows);
			doc.lineChart("Tendencia por día", new ArrayList<>(byDay.keySet()), new ArrayList<>(byDay.values()));
		}
		List<PartnerConsumption> detail = topRecent(rows, 40);
		doc.heading("Detalle (mostrando " + detail.size() + " de " + count + ")");
		List<String[]> table = new ArrayList<>();
		for (PartnerConsumption c : detail) {
			table.add(new String[]{fmt(c.getConsumptionOpening()), c.getEnviroment(), c.getTable(), c.getWaiterName(), money(total(c))});
		}
		doc.table(new String[]{"Apertura", "Ambiente", "Mesa", "Mesero", "Total"}, table);
		doc.total("Total general", money(total));
		addTopProducts(doc, productMetrics.top(from, to, 10));
		addProductPerformance(doc, productMetrics.categoryMix(from, to), productMetrics.productDetail(from, to));
		addEnvironmentPerformance(doc, productMetrics.byEnvironmentCategory(from, to));
		return doc.build();
	}

	public byte[] incomeByEnvironmentPdf(LocalDateTime from, LocalDateTime to) {
		List<EnvironmentTotalDTO> env = consumptionMetrics.byEnvironment(from, to);
		PdfSupport doc = new PdfSupport("Ingresos por ambiente", from, to);
		double total = 0.0;
		for (EnvironmentTotalDTO e : env) {
			total += e.getTotal();
		}
		doc.kpis(List.of(
				new String[]{"Total facturado", money(total)},
				new String[]{"Ambientes", String.valueOf(env.size())}));
		if (!env.isEmpty()) {
			List<String> cats = new ArrayList<>();
			List<Double> vals = new ArrayList<>();
			for (EnvironmentTotalDTO e : env) {
				cats.add(e.getEnvironment());
				vals.add(e.getTotal());
			}
			doc.barChart("Total por ambiente", cats, vals);
		}
		List<String[]> table = new ArrayList<>();
		for (EnvironmentTotalDTO e : env) {
			table.add(new String[]{e.getEnvironment(), String.valueOf(e.getCount()), money(e.getTotal()),
					String.format("%.0f%%", e.getPercentage())});
		}
		doc.table(new String[]{"Ambiente", "N° cargos", "Total", "%"}, table);
		doc.total("Total", money(total));
		addTopProducts(doc, productMetrics.top(from, to, 10));
		addProductPerformance(doc, productMetrics.categoryMix(from, to), productMetrics.productDetail(from, to));
		addEnvironmentPerformance(doc, productMetrics.byEnvironmentCategory(from, to));
		return doc.build();
	}

	public byte[] partnerStatementPdf(String identification, LocalDateTime from, LocalDateTime to) {
		PersonPartner p = partnerService.getByIdentification(identification);
		if (p == null) {
			return null;
		}
		List<PartnerConsumption> rows = consumptionRepo.findByPartnerPersonIdAndConsumptionOpeningBetween(
				p.getPersonId(), from, to);
		PdfSupport doc = new PdfSupport("Estado de cuenta del socio", from, to);
		doc.paragraph("Socio: " + p.getFirstName() + " " + p.getLastName()
				+ "     Identificación: " + p.getIdentification()
				+ "     Acción: " + p.getShareNumber());
		double total = 0.0;
		for (PartnerConsumption c : rows) {
			total += total(c);
		}
		int count = rows.size();
		doc.kpis(List.of(
				new String[]{"Total consumido", money(total)},
				new String[]{"N° de consumos", String.valueOf(count)},
				new String[]{"Valor promedio", money(count > 0 ? total / count : 0.0)}));
		if (!rows.isEmpty()) {
			Map<String, Double> byEnv = sumByEnvironment(rows);
			doc.barChart("Consumo por ambiente", new ArrayList<>(byEnv.keySet()), new ArrayList<>(byEnv.values()));
		}
		List<PartnerConsumption> detail = topRecent(rows, 40);
		doc.heading("Detalle (mostrando " + detail.size() + " de " + count + ")");
		List<String[]> table = new ArrayList<>();
		for (PartnerConsumption c : detail) {
			table.add(new String[]{fmt(c.getConsumptionOpening()), c.getEnviroment(), c.getTable(), money(total(c))});
		}
		doc.table(new String[]{"Apertura", "Ambiente", "Mesa", "Total"}, table);
		doc.total("Total consumido", money(total));
		addTopProducts(doc, productMetrics.topByPartner(p.getPersonId(), from, to, 10));
		return doc.build();
	}

	public byte[] securityPdf(LocalDateTime from, LocalDateTime to) {
		Instant fromI = from.atZone(ZoneId.systemDefault()).toInstant();
		Instant toI = to.atZone(ZoneId.systemDefault()).toInstant();
		List<UserFailedCountDTO> failed = auditQuery.failedLoginsByUser(fromI, toI);
		List<AuditEvent> critical = auditQuery.criticalEvents(fromI, toI);
		PdfSupport doc = new PdfSupport("Reporte administrativo de seguridad", from, to);

		long totalFailed = 0L;
		for (UserFailedCountDTO u : failed) {
			totalFailed += u.getCount();
		}
		doc.kpis(List.of(
				new String[]{"Intentos fallidos", String.valueOf(totalFailed)},
				new String[]{"Usuarios con fallos", String.valueOf(failed.size())},
				new String[]{"Alertas críticas", String.valueOf(critical.size())}));

		if (!failed.isEmpty()) {
			List<UserFailedCountDTO> top = failed.size() > 15 ? failed.subList(0, 15) : failed;
			List<String> cats = new ArrayList<>();
			List<Double> vals = new ArrayList<>();
			for (UserFailedCountDTO u : top) {
				cats.add(u.getUsername());
				vals.add((double) u.getCount());
			}
			doc.barChart("Intentos fallidos por usuario (top 15)", cats, vals);
		}

		doc.heading("Intentos fallidos por usuario");
		List<String[]> failTable = new ArrayList<>();
		for (UserFailedCountDTO u : failed) {
			failTable.add(new String[]{u.getUsername(), String.valueOf(u.getCount())});
		}
		doc.table(new String[]{"Usuario", "Intentos"}, failTable);

		List<AuditEvent> critShown = critical.size() > 100 ? critical.subList(0, 100) : critical;
		doc.heading("Alertas críticas (mostrando " + critShown.size() + " de " + critical.size() + ")");
		List<String[]> critTable = new ArrayList<>();
		for (AuditEvent e : critShown) {
			String when = e.getTimestamp() == null ? "" : LocalDateTime.ofInstant(e.getTimestamp(), ZoneId.systemDefault()).format(DTF);
			critTable.add(new String[]{when, e.getEventType(), e.getUsername() == null ? "" : e.getUsername(),
					e.getDetail() == null ? "" : e.getDetail()});
		}
		doc.table(new String[]{"Fecha", "Tipo", "Usuario", "Detalle"}, critTable);
		return doc.build();
	}

	private void addTopProducts(PdfSupport doc, List<ProductRankDTO> top) {
		if (top.isEmpty()) {
			return;
		}
		doc.heading("Top productos");
		List<String[]> rows = new ArrayList<>();
		for (ProductRankDTO p : top) {
			rows.add(new String[]{p.getName(), String.valueOf(p.getQuantity()), money(p.getRevenue())});
		}
		doc.table(new String[]{"Producto", "Cantidad", "Ingresos"}, rows);
		doc.barChart("Ingresos por producto (top 10)",
				top.stream().map(ProductRankDTO::getName).toList(),
				top.stream().map(ProductRankDTO::getRevenue).toList());
	}

	private void addProductPerformance(PdfSupport doc, List<CategoryMixDTO> mix, List<ProductDetailDTO> detail) {
		if (!mix.isEmpty()) {
			doc.heading("Rendimiento por categoría");
			List<String[]> rows = new ArrayList<>();
			for (CategoryMixDTO m : mix) {
				rows.add(new String[]{ section(m.getCategory(), m.getSubcategory()), String.valueOf(m.getQuantity()),
						money(m.getRevenue()), String.format("%.0f%%", m.getPercentage()) });
			}
			doc.table(new String[]{"Categoría", "Unidades", "Ingresos", "%"}, rows);
		}
		if (!detail.isEmpty()) {
			doc.heading("Detalle de productos por sección");
			List<String[]> rows = new ArrayList<>();
			for (ProductDetailDTO d : detail) {
				rows.add(new String[]{ section(d.getCategory(), d.getSubcategory()), d.getName(),
						String.valueOf(d.getQuantity()), money(d.getRevenue()) });
			}
			doc.table(new String[]{"Sección", "Producto", "Unidades", "Ingresos"}, rows);
		}
	}

	private void addEnvironmentPerformance(PdfSupport doc, List<EnvironmentCategoryDTO> rows) {
		if (rows.isEmpty()) {
			return;
		}
		doc.heading("Rendimiento por ambiente");
		List<String[]> table = new ArrayList<>();
		for (EnvironmentCategoryDTO r : rows) {
			String cat = (r.getCategory() == null || r.getCategory().isBlank()) ? "Sin categoría" : r.getCategory();
			table.add(new String[]{ r.getEnvironment(), cat, String.valueOf(r.getQuantity()), money(r.getRevenue()) });
		}
		doc.table(new String[]{"Ambiente", "Categoría", "Unidades", "Ingresos"}, table);
	}

	private String section(String category, String subcategory) {
		String c = (category == null || category.isBlank()) ? "Sin categoría" : category;
		String s = (subcategory == null || subcategory.isBlank()) ? "Sin categoría" : subcategory;
		return c + " · " + s;
	}

	private Map<String, Double> sumByEnvironment(List<PartnerConsumption> rows) {
		Map<String, Double> m = new LinkedHashMap<>();
		for (PartnerConsumption c : rows) {
			m.merge(c.getEnviroment() == null ? "—" : c.getEnviroment(), total(c), Double::sum);
		}
		return m;
	}

	private Map<String, Double> sumByDay(List<PartnerConsumption> rows) {
		Map<String, Double> m = new TreeMap<>();
		for (PartnerConsumption c : rows) {
			String day = c.getConsumptionOpening() == null ? "—" : c.getConsumptionOpening().toLocalDate().toString();
			m.merge(day, total(c), Double::sum);
		}
		return m;
	}

	private List<PartnerConsumption> topRecent(List<PartnerConsumption> rows, int n) {
		return rows.stream()
				.sorted(Comparator.comparing(PartnerConsumption::getConsumptionOpening,
						Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.limit(n)
				.toList();
	}

	private double total(PartnerConsumption c) {
		return safe(c.getConsumptionValue()) + safe(c.getIva()) + safe(c.getService()) + safe(c.getTip());
	}

	private double safe(Double d) {
		return d == null ? 0.0 : d;
	}

	private String money(double v) {
		return usd.format(v);
	}

	private String fmt(LocalDateTime dt) {
		return dt == null ? "" : dt.format(DTF);
	}
}
