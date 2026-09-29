package co.edu.unbosque.dto;

/**
 * Celda de la matriz día de la semana por hora de facturación.
 *
 * <p>Cruzar las dos dimensiones permite ver que el pico del viernes no está a la misma hora que el del martes, algo que las
 * series agregadas por separado no revelan.
 *
 * <p>El día se numera desde cero, con el lunes como cero. A diferencia de las series completas, aquí <strong>solo se emiten
 * las celdas con actividad</strong>: el cliente debe tratar las ausentes como cero al dibujar la matriz.
 */
public class PeakHeatmapCellDTO {

	private int weekday;
	private int hour;
	private double total;
	private long count;

	public PeakHeatmapCellDTO() {
	}

	public PeakHeatmapCellDTO(int weekday, int hour, double total, long count) {
		this.weekday = weekday;
		this.hour = hour;
		this.total = total;
		this.count = count;
	}

	public int getWeekday() {
		return weekday;
	}

	public void setWeekday(int v) {
		this.weekday = v;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int v) {
		this.hour = v;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double v) {
		this.total = v;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long v) {
		this.count = v;
	}
}
