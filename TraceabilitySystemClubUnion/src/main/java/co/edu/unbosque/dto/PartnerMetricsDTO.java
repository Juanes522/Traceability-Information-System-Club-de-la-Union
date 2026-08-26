package co.edu.unbosque.dto;

import java.util.List;

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
