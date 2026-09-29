package co.edu.unbosque.dto;

/**
 * Cruce de un ambiente con una categoría de producto.
 *
 * <p>Permite distinguir un bar donde predomina la bebida de un restaurante donde predomina la comida: cruza una dimensión
 * del consumo con una del producto, algo que ninguna otra métrica hace.
 */
public class EnvironmentCategoryDTO {
	private String environment;
	private String category;
	private long quantity;
	private double revenue;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public EnvironmentCategoryDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param environment el ambiente del club
	 * @param category la categoría del producto
	 * @param quantity la cantidad
	 * @param revenue los ingresos generados
	 */
	public EnvironmentCategoryDTO(String environment, String category, long quantity, double revenue) {
		this.environment = environment;
		this.category = category;
		this.quantity = quantity;
		this.revenue = revenue;
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
	 * @param environment el ambiente del club
	 */
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	/**
	 * Devuelve la categoría del producto.
	 *
	 * @return la categoría del producto
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Establece la categoría del producto.
	 *
	 * @param category la categoría del producto
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Devuelve la cantidad.
	 *
	 * @return la cantidad
	 */
	public long getQuantity() {
		return quantity;
	}

	/**
	 * Establece la cantidad.
	 *
	 * @param quantity la cantidad
	 */
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	/**
	 * Devuelve los ingresos generados.
	 *
	 * @return los ingresos generados
	 */
	public double getRevenue() {
		return revenue;
	}

	/**
	 * Establece los ingresos generados.
	 *
	 * @param revenue los ingresos generados
	 */
	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}
}
