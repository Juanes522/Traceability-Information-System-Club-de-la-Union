package co.edu.unbosque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Línea de detalle en la creación de un consumo: un producto, su cantidad y su precio unitario.
 *
 * <p>El importe de la línea <strong>no se recibe</strong>: lo calcula el servicio multiplicando precio por cantidad, de
 * modo que el cliente no puede declarar un total incoherente con sus propios factores.
 *
 * <p>La clasificación del producto es texto libre y no se valida: no existe catálogo de productos ni de categorías contra
 * el que contrastarla.
 */
public class ConsumptionItemRequest {

	private String productId;
	@NotBlank
	private String name;
	@NotNull
	@Positive
	private Integer quantity;
	@NotNull
	@PositiveOrZero
	private Double unitPrice;
	private String category;
	private String subcategory;
	private String dishType;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ConsumptionItemRequest() {
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
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * Establece la cantidad.
	 *
	 * @param quantity la cantidad
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * Devuelve el precio unitario.
	 *
	 * @return el precio unitario
	 */
	public Double getUnitPrice() {
		return unitPrice;
	}

	/**
	 * Establece el precio unitario.
	 *
	 * @param unitPrice el precio unitario
	 */
	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
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
	 * Devuelve el tipo de plato.
	 *
	 * @return el tipo de plato
	 */
	public String getDishType() {
		return dishType;
	}

	/**
	 * Establece el tipo de plato.
	 *
	 * @param dishType el tipo de plato
	 */
	public void setDishType(String dishType) {
		this.dishType = dishType;
	}
}
