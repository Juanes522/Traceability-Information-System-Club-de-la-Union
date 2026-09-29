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

	public EnvironmentTotalDTO() {
	}

	public EnvironmentTotalDTO(String environment, double total, long count, double percentage) {
		this.environment = environment;
		this.total = total;
		this.count = count;
		this.percentage = percentage;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String v) {
		this.environment = v;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double v) {
		this.total = v;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long v) {
		this.count = v;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double v) {
		this.percentage = v;
	}
}
