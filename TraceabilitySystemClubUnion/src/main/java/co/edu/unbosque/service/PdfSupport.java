package co.edu.unbosque.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Constructor de documentos PDF con la identidad visual del club.
 *
 * <p>Encapsula OpenPDF y JFreeChart detrás de un puñado de operaciones de alto nivel —encabezado, párrafo, tabla,
 * indicadores, gráfica— para que {@link ReportService} decida el contenido sin ocupar sitio en el detalle del dibujo.
 *
 * <p><strong>No es un bean de Spring.</strong> Se instancia una vez por reporte, y esa decisión es necesaria: el objeto
 * mantiene el estado del documento en construcción, de modo que compartirlo entre peticiones lo corrompería.
 *
 * <p>Particularidades del ciclo de vida que conviene conocer antes de usarla:
 *
 * <ul>
 *   <li><strong>El constructor ya realiza entrada/salida:</strong> abre el documento y escribe el encabezado de marca.
 *       No es un constructor inerte.</li>
 *   <li><strong>El objeto es de un solo uso.</strong> {@link #build()} cierra el documento; después no se puede añadir
 *       nada más.</li>
 *   <li><strong>Requiere una JVM capaz de operar sin entorno gráfico.</strong> Las gráficas se rasterizan con las
 *       bibliotecas de imagen y tipografía de AWT antes de incrustarse, lo que en un servidor sin pantalla exige el
 *       modo <em>headless</em>.</li>
 * </ul>
 *
 * <p>La paleta y la tipografía están fijadas en la clase: azul de marca para los títulos, dorado para los totales, y
 * un sombreado alterno en las filas de las tablas.
 *
 * @see ReportService
 */
public class PdfSupport {

	private static final Color BRAND = new Color(0x1A, 0x1F, 0x4D);
	private static final Color GOLD = new Color(0xC7, 0xA5, 0x67);
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final Document document;
	private final ByteArrayOutputStream out;

	/**
	 * Abre un documento nuevo y escribe el encabezado de marca.
	 *
	 * <p>Escribe el nombre del club, el título del reporte y una línea con el periodo cubierto y la fecha de generación.
	 * Esa línea de metadatos importa: sin ella un PDF descargado pierde el contexto de a qué periodo corresponde.
	 *
	 * <p><strong>Realiza entrada/salida de inmediato</strong>, de modo que un fallo al crear el documento se manifiesta
	 * aquí y no en {@link #build()}.
	 *
	 * @param reportTitle título del reporte, que aparece bajo el nombre del club
	 * @param from inicio del periodo cubierto, para la línea de metadatos
	 * @param to fin del periodo cubierto
	 */
	public PdfSupport(String reportTitle, LocalDateTime from, LocalDateTime to) {
		this.document = new Document(PageSize.A4, 40, 40, 54, 40);
		this.out = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, out);
		document.open();
		Paragraph club = new Paragraph("Club de la Unión",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND));
		document.add(club);
		Paragraph title = new Paragraph(reportTitle,
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.DARK_GRAY));
		document.add(title);
		Paragraph meta = new Paragraph(
				"Periodo: " + from.format(DTF) + " — " + to.format(DTF)
						+ "     Generado: " + LocalDateTime.now().format(DTF),
				FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY));
		meta.setSpacingAfter(12);
		document.add(meta);
	}

	/**
	 * Anade un titulo de seccion al documento.
	 *
	 * @param text texto del titulo
	 */
	public void heading(String text) {
		Paragraph h = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND));
		h.setSpacingBefore(10);
		h.setSpacingAfter(4);
		document.add(h);
	}

	/**
	 * Anade un parrafo de texto corrido al documento.
	 *
	 * @param text contenido del parrafo
	 */
	public void paragraph(String text) {
		Paragraph p = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY));
		p.setSpacingAfter(6);
		document.add(p);
	}

	/**
	 * Añade una tabla con encabezados y sombreado alterno de filas.
	 *
	 * <p>Ante una lista vacía <strong>no omite la tabla</strong>: dibuja una celda que indica la ausencia de datos en el
	 * periodo. Es una decisión acertada para un reporte, donde una sección que desaparece se confunde con un error de
	 * generación.
	 *
	 * @param headers títulos de las columnas
	 * @param rows filas, cada una con tantos elementos como encabezados
	 */
	public void table(String[] headers, List<String[]> rows) {
		PdfPTable table = new PdfPTable(headers.length);
		table.setWidthPercentage(100);
		table.setSpacingBefore(4);
		table.setSpacingAfter(8);
		Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
		for (String h : headers) {
			PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
			cell.setBackgroundColor(BRAND);
			cell.setPadding(5);
			table.addCell(cell);
		}
		Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
		boolean alt = false;
		for (String[] row : rows) {
			for (String value : row) {
				PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, bodyFont));
				cell.setPadding(4);
				if (alt) {
					cell.setBackgroundColor(new Color(0xF6, 0xF5, 0xF2));
				}
				table.addCell(cell);
			}
			alt = !alt;
		}
		if (rows.isEmpty()) {
			PdfPCell empty = new PdfPCell(new Phrase("Sin datos en el periodo", bodyFont));
			empty.setColspan(headers.length);
			empty.setPadding(6);
			table.addCell(empty);
		}
		document.add(table);
	}

	/**
	 * Anade una linea de total, alineada a la derecha y destacada.
	 *
	 * <p>Se usa para cerrar una tabla de detalle con su suma, de modo que la cifra agregada quede visualmente separada de las
	 * filas que la componen.
	 *
	 * @param label  etiqueta del total
	 * @param value  importe ya formateado
	 */
	public void total(String label, String value) {
		Paragraph p = new Paragraph(label + ": " + value,
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, GOLD.darker()));
		p.setAlignment(Element.ALIGN_RIGHT);
		p.setSpacingBefore(4);
		document.add(p);
	}

	/**
	 * Añade una fila de indicadores, cada uno con su etiqueta y su valor destacado.
	 *
	 * <p>Distribuye los indicadores en columnas sin bordes, a lo ancho de la página.
	 *
	 * <p><strong>No admite una lista vacía:</strong> construir una tabla de cero columnas falla. Todos los puntos de
	 * llamada actuales pasan entre dos y tres indicadores, así que la situación no se da, pero conviene saberlo al
	 * añadir un reporte nuevo.
	 *
	 * @param pairs pares de etiqueta y valor, ya formateados
	 */
	public void kpis(List<String[]> pairs) {
		PdfPTable table = new PdfPTable(pairs.size());
		table.setWidthPercentage(100);
		table.setSpacingBefore(4);
		table.setSpacingAfter(10);
		Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.GRAY);
		Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BRAND);
		for (String[] pair : pairs) {
			PdfPCell cell = new PdfPCell();
			cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			cell.setPadding(8);
			cell.addElement(new Paragraph(pair[0].toUpperCase(), labelFont));
			cell.addElement(new Paragraph(pair[1], valueFont));
			table.addCell(cell);
		}
		document.add(table);
	}

	/**
	 * Anade una grafica de barras al documento.
	 *
	 * <p>La grafica se rasteriza a imagen antes de incrustarse, de modo que su generacion requiere una maquina virtual capaz
	 * de operar sin entorno grafico.
	 *
	 * <p>Con una lista de categorias vacia no hace nada, en lugar de fallar: un periodo sin actividad debe producir igualmente
	 * un documento valido.
	 *
	 * @param title      titulo de la grafica
	 * @param categories etiquetas del eje de categorias
	 * @param values     valores correspondientes a cada categoria
	 */
	public void barChart(String title, List<String> categories, List<Double> values) {
		if (categories.isEmpty()) {
			return;
		}
		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		for (int i = 0; i < categories.size(); i++) {
			ds.addValue(values.get(i), "s", categories.get(i));
		}
		JFreeChart chart = ChartFactory.createBarChart(title, "", "", ds, PlotOrientation.VERTICAL, false, false, false);
		styleCategory(chart, GOLD);
		BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		addChart(chart);
	}

	/**
	 * Anade una grafica de linea al documento.
	 *
	 * <p>Se usa para las series temporales, donde la continuidad entre puntos es significativa, frente a la de barras, que
	 * compara categorias independientes.
	 *
	 * <p>Como aquella, no hace nada ante una lista de categorias vacia.
	 *
	 * @param title      titulo de la grafica
	 * @param categories etiquetas del eje temporal
	 * @param values     valores correspondientes a cada punto
	 */
	public void lineChart(String title, List<String> categories, List<Double> values) {
		if (categories.isEmpty()) {
			return;
		}
		DefaultCategoryDataset ds = new DefaultCategoryDataset();
		for (int i = 0; i < categories.size(); i++) {
			ds.addValue(values.get(i), "s", categories.get(i));
		}
		JFreeChart chart = ChartFactory.createLineChart(title, "", "", ds, PlotOrientation.VERTICAL, false, false, false);
		styleCategory(chart, BRAND);
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) chart.getCategoryPlot().getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2f));
		addChart(chart);
	}

	private void styleCategory(JFreeChart chart, Color series) {
		chart.setBackgroundPaint(Color.WHITE);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(new Color(0xE6, 0xE2, 0xD8));
		plot.setOutlineVisible(false);
		plot.getRenderer().setSeriesPaint(0, series);
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		domainAxis.setMaximumCategoryLabelWidthRatio(5.0f);
		domainAxis.setTickLabelFont(new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 11));
		plot.getRangeAxis().setTickLabelFont(new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 11));
		if (chart.getTitle() != null) {
			chart.getTitle().setFont(new java.awt.Font("Helvetica", java.awt.Font.BOLD, 12));
			chart.getTitle().setPaint(BRAND);
		}
	}

	private void addChart(JFreeChart chart) {
		try {
			BufferedImage bi = chart.createBufferedImage(1080, 540);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", baos);
			Image image = Image.getInstance(baos.toByteArray());
			image.scaleToFit(520, 260);
			image.setSpacingBefore(4);
			image.setSpacingAfter(10);
			document.add(image);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Cierra el documento y devuelve su contenido.
	 *
	 * <p><strong>Termina la vida útil del objeto:</strong> tras esta llamada no se puede añadir más contenido. Debe
	 * invocarse exactamente una vez, al final de la composición.
	 *
	 * @return el PDF completo, listo para enviarse como respuesta HTTP
	 */
	public byte[] build() {
		document.close();
		return out.toByteArray();
	}
}
