package co.edu.unbosque.dto;

/**
 * Posición de un producto en un ranking, con su cantidad e ingresos.
 *
 * <p>Lleva ambas magnitudes con independencia del criterio de orden aplicado, de modo que el cliente puede mostrar las dos
 * sin pedir otra vez los datos al cambiar de criterio.
 */
public class ProductRankDTO {
	private String productId;
	private String name;
	private long quantity;
	private double revenue;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ProductRankDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param productId el identificador del producto en el sistema de origen
	 * @param name el nombre
	 * @param quantity la cantidad
	 * @param revenue los ingresos generados
	 */
	public ProductRankDTO(String productId, String name, long quantity, double revenue) {
		this.productId = productId;
		this.name = name;
		this.quantity = quantity;
		this.revenue = revenue;
	}

	/**
	 * Devuelve el identificador del producto en el sistema de origen.
	 *
	 * @return el identificador del producto en el sistema de origen
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * Establece el identificador del producto en el sistema de origen.
	 *
	 * @param productId el identificador del producto en el sistema de origen
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * Devuelve el nombre.
	 *
	 * @return el nombre
	 */
	public String getName() {
		return name;
	}

	/**
	 * Establece el nombre.
	 *
	 * @param name el nombre
	 */
	public void setName(String name) {
		this.name = name;
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
