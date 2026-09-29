package co.edu.unbosque.dto;

/**
 * Producto individual con su clasificación completa, cantidad e ingresos.
 *
 * <p>Es la granularidad más fina de la analítica de producto. Solo la consumen los reportes en PDF, donde el orden de las
 * filas —por categoría, subcategoría y luego ingresos— <strong>es</strong> la estructura de la sección del documento.
 *
 * <p>Ningún endpoint la expone.
 */
public class ProductDetailDTO {
	private String category;
	private String subcategory;
	private String productId;
	private String name;
	private long quantity;
	private double revenue;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ProductDetailDTO() {
	}

	/**
	 * Crea el detalle de un producto con su clasificacion completa.
	 *
	 * @param category    categoria del producto
	 * @param subcategory subcategoria del producto
	 * @param productId   identificador del producto en el sistema de origen
	 * @param name        nombre del producto
	 * @param quantity    unidades vendidas en el periodo
	 * @param revenue     ingresos generados en el periodo
	 */
	public ProductDetailDTO(String category, String subcategory, String productId, String name, long quantity,
			double revenue) {
		this.category = category;
		this.subcategory = subcategory;
		this.productId = productId;
		this.name = name;
		this.quantity = quantity;
		this.revenue = revenue;
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
