package co.edu.unbosque.dto;

/**
 * Facturación de un ambiente, con su peso porcentual sobre el total del periodo.
 *
 * <p>El porcentaje no lo calcula la base: requiere una segunda pasada sobre el resultado, una vez conocido el total global.
 */
public class EnvironmentTotalDTO {

	private String environment;
	private double total;
	private long count;
	private double percentage;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public EnvironmentTotalDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param environment el ambiente del club
	 * @param total el importe total
	 * @param count el número de elementos agregados
	 * @param percentage el peso porcentual sobre el total del periodo
	 */
	public EnvironmentTotalDTO(String environment, double total, long count, double percentage) {
		this.environment = environment;
		this.total = total;
		this.count = count;
		this.percentage = percentage;
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
	 * Devuelve el importe total.
	 *
	 * @return el importe total
	 */
	public double getTotal() {
		return total;
	}

	/**
	 * Establece el importe total.
	 *
	 * @param v el importe total
	 */
	public void setTotal(double v) {
		this.total = v;
	}

	/**
	 * Devuelve el número de elementos agregados.
	 *
	 * @return el número de elementos agregados
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Establece el número de elementos agregados.
	 *
	 * @param v el número de elementos agregados
	 */
	public void setCount(long v) {
		this.count = v;
	}

	/**
	 * Devuelve el peso porcentual sobre el total del periodo.
	 *
	 * @return el peso porcentual sobre el total del periodo
	 */
	public double getPercentage() {
		return percentage;
	}

	/**
	 * Establece el peso porcentual sobre el total del periodo.
	 *
	 * @param v el peso porcentual sobre el total del periodo
	 */
	public void setPercentage(double v) {
		this.percentage = v;
	}
}
