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

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public PeakHeatmapCellDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param weekday el día de la semana
	 * @param hour la hora del día
	 * @param total el importe total
	 * @param count el número de elementos agregados
	 */
	public PeakHeatmapCellDTO(int weekday, int hour, double total, long count) {
		this.weekday = weekday;
		this.hour = hour;
		this.total = total;
		this.count = count;
	}

	/**
	 * Devuelve el día de la semana.
	 *
	 * @return el día de la semana
	 */
	public int getWeekday() {
		return weekday;
	}

	/**
	 * Establece el día de la semana.
	 *
	 * @param v el día de la semana
	 */
	public void setWeekday(int v) {
		this.weekday = v;
	}

	/**
	 * Devuelve la hora del día.
	 *
	 * @return la hora del día
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * Establece la hora del día.
	 *
	 * @param v la hora del día
	 */
	public void setHour(int v) {
		this.hour = v;
	}

	/**
	 * Devuelve el importe total.
	 *
	 * @return el importe total
	 */
	public double getTotal() {
		return total;
	}

	/**
	 * Establece el importe total.
	 *
	 * @param v el importe total
	 */
	public void setTotal(double v) {
		this.total = v;
	}

	/**
	 * Devuelve el número de elementos agregados.
	 *
	 * @return el número de elementos agregados
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Establece el número de elementos agregados.
	 *
	 * @param v el número de elementos agregados
	 */
	public void setCount(long v) {
		this.count = v;
	}
}
