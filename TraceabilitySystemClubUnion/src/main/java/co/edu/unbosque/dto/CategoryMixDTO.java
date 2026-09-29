package co.edu.unbosque.dto;

/**
 * Participación de una categoría y subcategoría en la venta del periodo.
 *
 * <p>Como en la distribución por ambiente, el porcentaje requiere una segunda pasada sobre el resultado, una vez conocido el
 * total global.
 *
 * <p>Las líneas sin clasificar se agrupan bajo una etiqueta genérica en lugar de descartarse.
 */
public class CategoryMixDTO {
	private String category;
	private String subcategory;
	private long quantity;
	private double revenue;
	private double percentage;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public CategoryMixDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param category la categoría del producto
	 * @param subcategory la subcategoria del producto
	 * @param quantity la cantidad
	 * @param revenue los ingresos generados
	 * @param percentage el peso porcentual sobre el total del periodo
	 */
	public CategoryMixDTO(String category, String subcategory, long quantity, double revenue, double percentage) {
		this.category = category;
		this.subcategory = subcategory;
		this.quantity = quantity;
		this.revenue = revenue;
		this.percentage = percentage;
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
	 * Devuelve la subcategoria del producto.
	 *
	 * @return la subcategoria del producto
	 */
	public String getSubcategory() {
		return subcategory;
	}

	/**
	 * Establece la subcategoria del producto.
	 *
	 * @param subcategory la subcategoria del producto
	 */
	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
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
	 * @param percentage el peso porcentual sobre el total del periodo
	 */
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}
}
