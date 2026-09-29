package co.edu.unbosque.dto;

/**
 * Indicadores de afluencia de un periodo.
 *
 * <p>La frecuencia media es el cociente entre visitas y socios distintos, es decir cuántas veces vuelve en promedio quien
 * viene.
 *
 * <p><strong>Advertencia sobre el número de presentes:</strong> a diferencia de los demás campos, ignora el periodo
 * solicitado y se refiere siempre al momento actual. En una consulta histórica es un dato en vivo mezclado con datos del
 * pasado, y nada en la respuesta lo distingue.
 *
 * <p>Recuérdese además que estas visitas se infieren del registro de consumos, no de un control de entrada: miden actividad
 * de consumo, no presencia física.
 */
public class AccessSummaryDTO {

	private long presentNow;
	private long visits;
	private long uniquePartners;
	private double avgFrequency;

	public long getPresentNow() {
		return presentNow;
	}

	public void setPresentNow(long v) {
		this.presentNow = v;
	}

	public long getVisits() {
		return visits;
	}

	public void setVisits(long v) {
		this.visits = v;
	}

	public long getUniquePartners() {
		return uniquePartners;
	}

	public void setUniquePartners(long v) {
		this.uniquePartners = v;
	}

	public double getAvgFrequency() {
		return avgFrequency;
	}

	public void setAvgFrequency(double v) {
		this.avgFrequency = v;
	}
}
