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

	/**
	 * Devuelve el número de socios presentes en este momento, con independencia del periodo consultado.
	 *
	 * @return el número de socios presentes en este momento, con independencia del periodo consultado
	 */
	public long getPresentNow() {
		return presentNow;
	}

	/**
	 * Establece el número de socios presentes en este momento, con independencia del periodo consultado.
	 *
	 * @param v el número de socios presentes en este momento, con independencia del periodo consultado
	 */
	public void setPresentNow(long v) {
		this.presentNow = v;
	}

	/**
	 * Devuelve el número de visitas.
	 *
	 * @return el número de visitas
	 */
	public long getVisits() {
		return visits;
	}

	/**
	 * Establece el número de visitas.
	 *
	 * @param v el número de visitas
	 */
	public void setVisits(long v) {
		this.visits = v;
	}

	/**
	 * Devuelve el número de socios distintos del periodo.
	 *
	 * @return el número de socios distintos del periodo
	 */
	public long getUniquePartners() {
		return uniquePartners;
	}

	/**
	 * Establece el número de socios distintos del periodo.
	 *
	 * @param v el número de socios distintos del periodo
	 */
	public void setUniquePartners(long v) {
		this.uniquePartners = v;
	}

	/**
	 * Devuelve la frecuencia media de visita por socio.
	 *
	 * @return la frecuencia media de visita por socio
	 */
	public double getAvgFrequency() {
		return avgFrequency;
	}

	/**
	 * Establece la frecuencia media de visita por socio.
	 *
	 * @param v la frecuencia media de visita por socio
	 */
	public void setAvgFrequency(double v) {
		this.avgFrequency = v;
	}
}
