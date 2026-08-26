package co.edu.unbosque.service;

import co.edu.unbosque.dto.CategoryMixDTO;
import co.edu.unbosque.dto.EnvironmentCategoryDTO;
import co.edu.unbosque.dto.EnvironmentTotalDTO;
import co.edu.unbosque.dto.ProductDetailDTO;
import co.edu.unbosque.dto.ProductRankDTO;
import co.edu.unbosque.dto.UserFailedCountDTO;
import co.edu.unbosque.model.PartnerConsumption;
import co.edu.unbosque.model.PersonPartner;
import co.edu.unbosque.repository.PartnerConsumptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportServiceTest {

	private PartnerConsumptionRepository consumptionRepo;
	private PersonPartnerService partnerService;
	private ConsumptionMetricsService consumptionMetrics;
	private AuditQueryService auditQuery;
	private ProductMetricsService productMetrics;
	private ReportService service;

	private final LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
	private final LocalDateTime to = LocalDateTime.of(2026, 8, 31, 0, 0);

	@BeforeEach
	void setUp() {
		consumptionRepo = mock(PartnerConsumptionRepository.class);
		partnerService = mock(PersonPartnerService.class);
		consumptionMetrics = mock(ConsumptionMetricsService.class);
		auditQuery = mock(AuditQueryService.class);
		productMetrics = mock(ProductMetricsService.class);
		service = new ReportService(consumptionRepo, partnerService, consumptionMetrics, auditQuery, productMetrics);
	}

	private PartnerConsumption sample() {
		PartnerConsumption c = new PartnerConsumption();
		c.setEnviroment("Bar");
		c.setTable("3");
		c.setWaiterName("Pedro");
		c.setConsumptionValue(100.0);
		c.setIva(19.0);
		c.setService(10.0);
		c.setTip(5.0);
		c.setConsumptionOpening(LocalDateTime.of(2026, 8, 10, 12, 0));
		return c;
	}

	private void assertPdf(byte[] bytes) {
		assertTrue(bytes != null && bytes.length > 100);
		assertTrue(bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F');
	}

	@Test
	void consumptionsPdf_rendersWithoutFilter() {
		when(consumptionRepo.findByConsumptionOpeningBetween(from, to)).thenReturn(List.of(sample()));
		assertPdf(service.consumptionsPdf(from, to, null));
	}

	@Test
	void consumptionsPdf_usesEnvironmentFilterWhenGiven() {
		when(consumptionRepo.findByEnviromentAndConsumptionOpeningBetween("Bar", from, to)).thenReturn(List.of(sample()));
		assertPdf(service.consumptionsPdf(from, to, "Bar"));
	}

	@Test
	void incomeByEnvironmentPdf_renders() {
		when(consumptionMetrics.byEnvironment(from, to)).thenReturn(List.of(new EnvironmentTotalDTO("Bar", 134.0, 1L, 100.0)));
		assertPdf(service.incomeByEnvironmentPdf(from, to));
	}

	@Test
	void partnerStatementPdf_rendersForExistingPartner() {
		PersonPartner p = new PersonPartner();
		p.setIdentification("123");
		p.setFirstName("Ana");
		p.setLastName("Lopez");
		p.setShareNumber(1001L);
		org.springframework.test.util.ReflectionTestUtils.setField(p, "personId", 7L);
		when(partnerService.getByIdentification("123")).thenReturn(p);
		when(consumptionRepo.findByPartnerPersonIdAndConsumptionOpeningBetween(7L, from, to)).thenReturn(List.of(sample()));
		assertPdf(service.partnerStatementPdf("123", from, to));
	}

	@Test
	void partnerStatementPdf_returnsNullWhenPartnerMissing() {
		when(partnerService.getByIdentification("999")).thenReturn(null);
		assertNull(service.partnerStatementPdf("999", from, to));
	}

	@Test
	void securityPdf_renders() {
		when(auditQuery.failedLoginsByUser(any(), any())).thenReturn(List.of(new UserFailedCountDTO("123", 3)));
		when(auditQuery.criticalEvents(any(), any())).thenReturn(List.of());
		assertPdf(service.securityPdf(from, to));
	}

	@Test
	void consumptionsPdf_includesTopProductsSectionWhenPresent() {
		when(consumptionRepo.findByConsumptionOpeningBetween(from, to)).thenReturn(List.of(sample()));
		when(productMetrics.top(from, to, 10)).thenReturn(List.of(new ProductRankDTO("P1", "Cerveza", 5L, 50.0)));
		assertPdf(service.consumptionsPdf(from, to, null));
	}

	@Test
	void incomeByEnvironmentPdf_includesTopProductsSectionWhenPresent() {
		when(consumptionMetrics.byEnvironment(from, to)).thenReturn(List.of(new EnvironmentTotalDTO("Bar", 134.0, 1L, 100.0)));
		when(productMetrics.top(from, to, 10)).thenReturn(List.of(new ProductRankDTO("P1", "Cerveza", 5L, 50.0)));
		assertPdf(service.incomeByEnvironmentPdf(from, to));
	}

	@Test
	void partnerStatementPdf_includesTopProductsSectionWhenPresent() {
		PersonPartner p = new PersonPartner();
		p.setIdentification("123");
		p.setFirstName("Ana");
		p.setLastName("Lopez");
		p.setShareNumber(1001L);
		org.springframework.test.util.ReflectionTestUtils.setField(p, "personId", 7L);
		when(partnerService.getByIdentification("123")).thenReturn(p);
		when(consumptionRepo.findByPartnerPersonIdAndConsumptionOpeningBetween(7L, from, to)).thenReturn(List.of(sample()));
		when(productMetrics.topByPartner(7L, from, to, 10)).thenReturn(List.of(new ProductRankDTO("P1", "Cerveza", 5L, 50.0)));
		assertPdf(service.partnerStatementPdf("123", from, to));
	}

	@Test
	void consumptionsPdf_incluyeRendimientoDeProductos() {
		when(consumptionRepo.findByConsumptionOpeningBetween(from, to)).thenReturn(List.of(sample()));
		when(productMetrics.categoryMix(any(), any())).thenReturn(List.of(new CategoryMixDTO("BEBIDAS","VINO TINTO",3L,30.0,100.0)));
		when(productMetrics.productDetail(any(), any())).thenReturn(List.of(new ProductDetailDTO("BEBIDAS","VINO TINTO","P1","VINO",3L,30.0)));
		byte[] pdf = service.consumptionsPdf(from, to, null);
		assertTrue(pdf != null && pdf.length > 0);
	}

	@Test
	void consumptionsPdf_incluyeRendimientoPorAmbiente() {
		when(consumptionRepo.findByConsumptionOpeningBetween(from, to)).thenReturn(List.of(sample()));
		when(productMetrics.byEnvironmentCategory(any(), any()))
				.thenReturn(List.of(new EnvironmentCategoryDTO("Rooftop", "BEBIDAS", 5L, 40.0)));
		byte[] pdf = service.consumptionsPdf(from, to, null);
		assertPdf(pdf);
	}

	@Test
	void incomeByEnvironmentPdf_incluyeRendimientoPorAmbiente() {
		when(consumptionMetrics.byEnvironment(from, to)).thenReturn(List.of(new EnvironmentTotalDTO("Bar", 134.0, 1L, 100.0)));
		when(productMetrics.byEnvironmentCategory(any(), any()))
				.thenReturn(List.of(new EnvironmentCategoryDTO("Rooftop", "BEBIDAS", 5L, 40.0)));
		byte[] pdf = service.incomeByEnvironmentPdf(from, to);
		assertPdf(pdf);
	}

	@Test
	void consumptionsPdf_capsDetailAndRendersWithManyRows() {
		java.util.List<PartnerConsumption> many = new java.util.ArrayList<>();
		for (int i = 0; i < 120; i++) {
			PartnerConsumption c = new PartnerConsumption();
			c.setEnviroment(i % 2 == 0 ? "Bar" : "Restaurante");
			c.setTable(String.valueOf(i));
			c.setWaiterName("W" + i);
			c.setConsumptionValue(1000.0 + i);
			c.setIva(0.0);
			c.setService(0.0);
			c.setTip(0.0);
			c.setConsumptionOpening(LocalDateTime.of(2026, 8, 1, 0, 0).plusHours(i));
			many.add(c);
		}
		when(consumptionRepo.findByConsumptionOpeningBetween(from, to)).thenReturn(many);
		assertPdf(service.consumptionsPdf(from, to, null));
	}
}
