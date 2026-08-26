package co.edu.unbosque.dto;

public class EnvironmentOccupancyDTO {

	private String environment;
	private long partners;

	public EnvironmentOccupancyDTO() {
	}

	public EnvironmentOccupancyDTO(String environment, long partners) {
		this.environment = environment;
		this.partners = partners;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String v) {
		this.environment = v;
	}

	public long getPartners() {
		return partners;
	}

	public void setPartners(long v) {
		this.partners = v;
	}
}
