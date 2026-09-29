package co.edu.unbosque.dto;

/**
 * Número de socios distintos que consumieron hoy en un ambiente.
 *
 * <p>Es lo que el sistema llama «ocupación», y conviene precisarlo: se deriva de los <strong>consumos</strong> y no de la
 * tabla de accesos, de modo que es una medida de actividad por espacio, no de aforo.
 */
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
