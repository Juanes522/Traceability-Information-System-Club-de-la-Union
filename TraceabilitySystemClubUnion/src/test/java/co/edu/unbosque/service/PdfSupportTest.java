package co.edu.unbosque.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfSupportTest {

	@Test
	void build_producesPdfBytes() {
		PdfSupport doc = new PdfSupport("Reporte de prueba",
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		doc.table(new String[]{"A", "B"}, List.of(new String[]{"1", "2"}, new String[]{"3", "4"}));
		doc.total("Total", "6");
		byte[] bytes = doc.build();

		assertTrue(bytes.length > 100);
		assertTrue(bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F',
				"debe empezar con la firma %PDF");
	}

	@Test
	void build_supportsMultipleSections() {
		PdfSupport doc = new PdfSupport("Seguridad",
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		doc.heading("Sección 1");
		doc.table(new String[]{"X"}, List.<String[]>of(new String[]{"a"}));
		doc.heading("Sección 2");
		doc.table(new String[]{"Y"}, List.of());
		byte[] bytes = doc.build();

		assertTrue(bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F');
	}

	@Test
	void kpisAndCharts_produceValidPdf() {
		PdfSupport doc = new PdfSupport("Con gráficas",
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		doc.kpis(List.of(new String[]{"Total", "$100"}, new String[]{"Cargos", "3"}));
		doc.barChart("Barras", List.of("Bar", "Restaurante"), List.of(100.0, 50.0));
		doc.lineChart("Línea", List.of("2026-08-01", "2026-08-02"), List.of(10.0, 20.0));
		byte[] bytes = doc.build();
		assertTrue(bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F');
	}

	@Test
	void charts_withEmptyData_doNotThrow() {
		PdfSupport doc = new PdfSupport("Vacío",
				LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 0, 0));
		doc.barChart("Barras", List.of(), List.of());
		doc.lineChart("Línea", List.of(), List.of());
		byte[] bytes = doc.build();
		assertTrue(bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F');
	}
}
