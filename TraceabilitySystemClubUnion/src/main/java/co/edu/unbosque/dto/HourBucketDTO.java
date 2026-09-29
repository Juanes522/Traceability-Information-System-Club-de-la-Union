package co.edu.unbosque.dto;

/**
 * Facturación agregada en una hora del día.
 *
 * <p>Se emiten siempre las veinticuatro horas, rellenando con ceros las franjas sin actividad.
 */
public class HourBucketDTO {

	private int hour;
	private double total;
	private long count;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public HourBucketDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param hour la hora del día
	 * @param total el importe total
	 * @param count el número de elementos agregados
	 */
	public HourBucketDTO(int hour, double total, long count) {
		this.hour = hour;
		this.total = total;
		this.count = count;
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
