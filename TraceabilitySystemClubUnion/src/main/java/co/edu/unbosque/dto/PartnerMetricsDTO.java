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

	public PartnerMetricsDTO() {
	}

	public PartnerMetricsDTO(ConsumptionSummaryDTO summary, List<EnvironmentTotalDTO> byEnvironment,
			List<TrendPointDTO> trend) {
		this.summary = summary;
		this.byEnvironment = byEnvironment;
		this.trend = trend;
	}

	public ConsumptionSummaryDTO getSummary() {
		return summary;
	}

	public void setSummary(ConsumptionSummaryDTO v) {
		this.summary = v;
	}

	public List<EnvironmentTotalDTO> getByEnvironment() {
		return byEnvironment;
	}

	public void setByEnvironment(List<EnvironmentTotalDTO> v) {
		this.byEnvironment = v;
	}

	public List<TrendPointDTO> getTrend() {
		return trend;
	}

	public void setTrend(List<TrendPointDTO> v) {
		this.trend = v;
	}

	public long getVisits() {
		return visits;
	}

	public void setVisits(long v) {
		this.visits = v;
	}

	public String getLastVisit() {
		return lastVisit;
	}

	public void setLastVisit(String v) {
		this.lastVisit = v;
	}
}
