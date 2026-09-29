package co.edu.unbosque.dto;

/**
 * Punto de una serie temporal de visitas.
 *
 * <p>Como las series de facturación, se devuelve sin huecos: los periodos sin visitas llegan en cero.
 */
public class AttendancePointDTO {

	private String bucket;
	private long count;

	public AttendancePointDTO() {
	}

	public AttendancePointDTO(String bucket, long count) {
		this.bucket = bucket;
		this.count = count;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String v) {
		this.bucket = v;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long v) {
		this.count = v;
	}
}
