package co.edu.unbosque.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Línea de detalle de un {@link PartnerConsumption}: un producto consumido, su cantidad y su precio.
 *
 * <p>Estas filas sostienen <strong>toda la analítica de productos</strong> del sistema: los rankings
 * de {@code /metrics/products/*}, la mezcla por categoría y las secciones de producto de los
 * reportes PDF se calculan agregando sobre esta tabla mediante
 * {@link co.edu.unbosque.repository.ConsumptionItemRepository}.
 *
 * <p>La clasificación en {@link #category}, {@link #subcategory} y {@link #dishType} es texto libre
 * sin catálogo: no hay entidad de producto ni enumeración que los restrinja. Las agregaciones
 * agrupan por el valor tal como se escribió, de modo que dos grafías distintas del mismo producto
 * producen dos filas distintas en los rankings.
 */
@Entity
@Table(name = "consumption_item")
public class ConsumptionItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Consumo al que pertenece la línea. Lado propietario de la clave ajena.
	 *
	 * <p>Es {@code @JsonIgnore} para no serializar el padre desde el hijo, pero nótese que el efecto
	 * combinado con el {@code @JsonIgnore} de {@code PartnerConsumption.items} es que
	 * <strong>las líneas nunca se exponen al cliente</strong> por ninguna vía.
	 */
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "consumption_id")
	private PartnerConsumption consumption;

	/** Identificador del producto en el sistema de origen. Texto libre, sin entidad de producto detrás. */
	private String productId;
	private String name;
	private Integer quantity;
	private Double unitPrice;
	/**
	 * Importe de la línea, almacenado de forma denormalizada.
	 *
	 * <p>Lo calcula {@link co.edu.unbosque.service.PartnerConsumptionService#register} como
	 * {@code unitPrice * quantity} y lo persiste; <strong>la entidad no lo deriva</strong>. Nada lo
	 * recalcula ni lo valida después, y nada comprueba que la suma de las líneas coincida con el
	 * {@code consumptionValue} del consumo padre.
	 *
	 * <p>Las agregaciones de ingresos por producto suman esta columna, de modo que un valor
	 * inconsistente se propaga silenciosamente a los rankings y a los reportes.
	 */
	private Double lineTotal;
	private String category;
	private String subcategory;

	@Column(name = "dish_type")
	private String dishType;

	/**
	 * Constructor sin argumentos requerido por el proveedor de persistencia.
	 *
	 * <p>No está pensado para usarse desde el código de la aplicación.
	 */
	public ConsumptionItem() {
	}

	/**
	 * Devuelve el identificador.
	 *
	 * @return el identificador
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador.
	 *
	 * @param id el identificador
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Devuelve el consumo asociado.
	 *
	 * @return el consumo asociado
	 */
	public PartnerConsumption getConsumption() {
		return consumption;
	}

	/**
	 * Establece el consumo asociado.
	 *
	 * @param consumption el consumo asociado
	 */
	public void setConsumption(PartnerConsumption consumption) {
		this.consumption = consumption;
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
	 * Devuelve el importe de la línea, calculado como precio por cantidad.
	 *
	 * @return el importe de la línea, calculado como precio por cantidad
	 */
	public Double getLineTotal() {
		return lineTotal;
	}

	/**
	 * Establece el importe de la línea, calculado como precio por cantidad.
	 *
	 * @param lineTotal el importe de la línea, calculado como precio por cantidad
	 */
	public void setLineTotal(Double lineTotal) {
		this.lineTotal = lineTotal;
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
