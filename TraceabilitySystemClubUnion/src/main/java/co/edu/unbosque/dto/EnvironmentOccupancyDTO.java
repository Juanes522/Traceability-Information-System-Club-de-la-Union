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

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public EnvironmentOccupancyDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param environment el ambiente del club
	 * @param partners el número de socios distintos
	 */
	public EnvironmentOccupancyDTO(String environment, long partners) {
		this.environment = environment;
		this.partners = partners;
	}

	/**
	 * Devuelve el ambiente del club.
	 *
	 * @return el ambiente del club
	 */
	public String getEnvironment() {
		return environment;
	}

	/**
	 * Establece el ambiente del club.
	 *
	 * @param v el ambiente del club
	 */
	public void setEnvironment(String v) {
		this.environment = v;
	}

	/**
	 * Devuelve el número de socios distintos.
	 *
	 * @return el número de socios distintos
	 */
	public long getPartners() {
		return partners;
	}

	/**
	 * Establece el número de socios distintos.
	 *
	 * @param v el número de socios distintos
	 */
	public void setPartners(long v) {
		this.partners = v;
	}
}
