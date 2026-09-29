package co.edu.unbosque.dto;

/**
 * Punto de una serie temporal de visitas.
 *
 * <p>Como las series de facturación, se devuelve sin huecos: los periodos sin visitas llegan en cero.
 */
public class AttendancePointDTO {

	private String bucket;
	private long count;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public AttendancePointDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param bucket la etiqueta del periodo de la serie
	 * @param count el número de elementos agregados
	 */
	public AttendancePointDTO(String bucket, long count) {
		this.bucket = bucket;
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
