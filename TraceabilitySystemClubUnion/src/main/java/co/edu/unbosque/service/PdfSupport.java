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

public class PdfSupport {

	private static final Color BRAND = new Color(0x1A, 0x1F, 0x4D);
	private static final Color GOLD = new Color(0xC7, 0xA5, 0x67);
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final Document document;
	private final ByteArrayOutputStream out;

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

	public void heading(String text) {
		Paragraph h = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND));
		h.setSpacingBefore(10);
		h.setSpacingAfter(4);
		document.add(h);
	}

	public void paragraph(String text) {
		Paragraph p = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY));
		p.setSpacingAfter(6);
		document.add(p);
	}

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

	public void total(String label, String value) {
		Paragraph p = new Paragraph(label + ": " + value,
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, GOLD.darker()));
		p.setAlignment(Element.ALIGN_RIGHT);
		p.setSpacingBefore(4);
		document.add(p);
	}

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

	public byte[] build() {
		document.close();
		return out.toByteArray();
	}
}
