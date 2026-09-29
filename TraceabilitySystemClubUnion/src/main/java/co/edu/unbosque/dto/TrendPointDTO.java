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

	public TrendPointDTO() {
	}

	public TrendPointDTO(String bucket, double total, long count) {
		this.bucket = bucket;
		this.total = total;
		this.count = count;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String v) {
		this.bucket = v;
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
