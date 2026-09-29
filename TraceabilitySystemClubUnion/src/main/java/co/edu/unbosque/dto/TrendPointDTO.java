package co.edu.unbosque.dto;

/**
 * Punto de una serie temporal de facturación.
 *
 * <p>La etiqueta del periodo es una cadena ya formateada según la granularidad pedida —día, semana o mes—, de modo que el
 * cliente la usa tal cual como rótulo del eje sin reinterpretarla.
 *
 * <p>Las series se devuelven <strong>sin huecos</strong>: los periodos sin actividad llegan en cero.
 */
public class TrendPointDTO {

	private String bucket;
	private double total;
	private long count;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public TrendPointDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param bucket la etiqueta del periodo de la serie
	 * @param total el importe total
	 * @param count el número de elementos agregados
	 */
	public TrendPointDTO(String bucket, double total, long count) {
		this.bucket = bucket;
		this.total = total;
		this.count = count;
	}

	/**
	 * Devuelve la etiqueta del periodo de la serie.
	 *
	 * @return la etiqueta del periodo de la serie
	 */
	public String getBucket() {
		return bucket;
	}

	/**
	 * Establece la etiqueta del periodo de la serie.
	 *
	 * @param v la etiqueta del periodo de la serie
	 */
	public void setBucket(String v) {
		this.bucket = v;
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
