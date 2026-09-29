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

	public ConsumptionItemRequest() {
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}

	public String getDishType() {
		return dishType;
	}

	public void setDishType(String dishType) {
		this.dishType = dishType;
	}
}
