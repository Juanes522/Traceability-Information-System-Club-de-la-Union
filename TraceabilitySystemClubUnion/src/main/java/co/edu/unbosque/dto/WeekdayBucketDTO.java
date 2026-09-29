package co.edu.unbosque.dto;

/**
 * Facturación agregada en un día de la semana.
 *
 * <p>Se emiten siempre los siete días, de lunes a domingo, rellenando con ceros los que no tuvieron actividad.
 */
public class WeekdayBucketDTO {

	private String weekday;
	private double total;
	private long count;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public WeekdayBucketDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param weekday el día de la semana
	 * @param total el importe total
	 * @param count el número de elementos agregados
	 */
	public WeekdayBucketDTO(String weekday, double total, long count) {
		this.weekday = weekday;
		this.total = total;
		this.count = count;
	}

	/**
	 * Devuelve el día de la semana.
	 *
	 * @return el día de la semana
	 */
	public String getWeekday() {
		return weekday;
	}

	/**
	 * Establece el día de la semana.
	 *
	 * @param v el día de la semana
	 */
	public void setWeekday(String v) {
		this.weekday = v;
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
