package co.edu.unbosque.dto;

import java.util.List;

/**
 * Conjunto completo de métricas de un socio: su facturación, sus ambientes, su tendencia y sus visitas.
 *
 * <p>Reutiliza los mismos DTO que las métricas globales, de modo que un mismo componente del cliente puede presentar el
 * consumo de un socio y el del club entero.
 *
 * <p><strong>Advertencia sobre la última visita:</strong> a diferencia de todos los demás campos, ignora el periodo
 * solicitado; es la última visita registrada del socio, que puede ser muy anterior a la ventana consultada. Y cuando el
 * socio nunca ha visitado el club se informa como cadena vacía, no como nulo.
 */
public class PartnerMetricsDTO {

	private ConsumptionSummaryDTO summary;
	private List<EnvironmentTotalDTO> byEnvironment;
	private List<TrendPointDTO> trend;
	private long visits;
	private String lastVisit;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public PartnerMetricsDTO() {
	}

	/**
	 * Crea las metricas del socio a partir de sus tres bloques agregados.
	 *
	 * <p>Las visitas y la fecha de ultima visita se anaden despues, mediante sus propios metodos, porque provienen de
	 * consultas distintas.
	 *
	 * @param summary       resumen de facturacion del socio
	 * @param byEnvironment desglose por ambiente
	 * @param trend         serie temporal de facturacion
	 */
	public PartnerMetricsDTO(ConsumptionSummaryDTO summary, List<EnvironmentTotalDTO> byEnvironment,
			List<TrendPointDTO> trend) {
		this.summary = summary;
		this.byEnvironment = byEnvironment;
		this.trend = trend;
	}

	/**
	 * Devuelve el resumen de facturacion.
	 *
	 * @return el resumen de facturacion
	 */
	public ConsumptionSummaryDTO getSummary() {
		return summary;
	}

	/**
	 * Establece el resumen de facturacion.
	 *
	 * @param v el resumen de facturacion
	 */
	public void setSummary(ConsumptionSummaryDTO v) {
		this.summary = v;
	}

	/**
	 * Devuelve el desglose por ambiente.
	 *
	 * @return el desglose por ambiente
	 */
	public List<EnvironmentTotalDTO> getByEnvironment() {
		return byEnvironment;
	}

	/**
	 * Establece el desglose por ambiente.
	 *
	 * @param v el desglose por ambiente
	 */
	public void setByEnvironment(List<EnvironmentTotalDTO> v) {
		this.byEnvironment = v;
	}

	/**
	 * Devuelve la serie temporal de facturacion.
	 *
	 * @return la serie temporal de facturacion
	 */
	public List<TrendPointDTO> getTrend() {
		return trend;
	}

	/**
	 * Establece la serie temporal de facturacion.
	 *
	 * @param v la serie temporal de facturacion
	 */
	public void setTrend(List<TrendPointDTO> v) {
		this.trend = v;
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
	 * Devuelve la fecha de la última visita registrada, que puede quedar fuera del periodo consultado.
	 *
	 * @return la fecha de la última visita registrada, que puede quedar fuera del periodo consultado
	 */
	public String getLastVisit() {
		return lastVisit;
	}

	/**
	 * Establece la fecha de la última visita registrada, que puede quedar fuera del periodo consultado.
	 *
	 * @param v la fecha de la última visita registrada, que puede quedar fuera del periodo consultado
	 */
	public void setLastVisit(String v) {
		this.lastVisit = v;
	}
}
