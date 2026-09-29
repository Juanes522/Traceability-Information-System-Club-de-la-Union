package co.edu.unbosque.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para registrar un consumo, con sus líneas de detalle.
 *
 * <p>Es el DTO más validado del sistema, y con razón: alimenta la única operación de escritura que mueve dinero.
 *
 * <p>Lo que <strong>sí</strong> se valida: el socio es obligatorio, el ambiente y el mesero no pueden ir vacíos, los
 * cuatro importes son obligatorios y no negativos, la hora de apertura es obligatoria, y cada línea se valida en cascada.
 *
 * <p>Lo que <strong>no</strong> se valida, y conviene conocer:
 *
 * <ul>
 *   <li><strong>No hay validación cruzada.</strong> Nada comprueba que el cierre sea posterior a la apertura, ni que la
 *       suma de las líneas coincida con el valor declarado del consumo, ni que la apertura esté en el pasado.</li>
 *   <li>La lista de líneas <strong>no tiene tope de tamaño ni exige contenido</strong>: se admite un consumo sin detalle,
 *       que quedaría fuera de toda la analítica de productos.</li>
 *   <li>La hora de cierre es opcional; si falta, el servicio inventa una a veinte minutos de la apertura.</li>
 * </ul>
 *
 * <p>El campo del ambiente conserva la errata del modelo, sin la segunda letra n, porque debe coincidir con el nombre que
 * usan la entidad y las consultas.
 */
public class ConsumptionCreateRequest {

	@NotNull
	private Long partnerId;
	@NotBlank
	@Size(max = 100)
	private String enviroment;
	private Integer account;
	@Size(max = 50)
	private String table;
	@NotBlank
	@Size(max = 100)
	private String waiterName;
	private Character isPartner;
	@NotNull
	@PositiveOrZero
	private Double consumptionValue;
	@NotNull
	@PositiveOrZero
	private Double iva;
	@NotNull
	@PositiveOrZero
	private Double service;
	@NotNull
	@PositiveOrZero
	private Double tip;
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime consumptionOpening;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime consumptionClosing;
	private List<@Valid ConsumptionItemRequest> items;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ConsumptionCreateRequest() {
	}

	/**
	 * Devuelve el identificador del socio.
	 *
	 * @return el identificador del socio
	 */
	public Long getPartnerId() {
		return partnerId;
	}

	/**
	 * Establece el identificador del socio.
	 *
	 * @param partnerId el identificador del socio
	 */
	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}

	/**
	 * Devuelve el ambiente del club donde se produjo el consumo.
	 *
	 * @return el ambiente del club donde se produjo el consumo
	 */
	public String getEnviroment() {
		return enviroment;
	}

	/**
	 * Establece el ambiente del club donde se produjo el consumo.
	 *
	 * @param enviroment el ambiente del club donde se produjo el consumo
	 */
	public void setEnviroment(String enviroment) {
		this.enviroment = enviroment;
	}

	/**
	 * Devuelve el número de cuenta del consumo.
	 *
	 * @return el número de cuenta del consumo
	 */
	public Integer getAccount() {
		return account;
	}

	/**
	 * Establece el número de cuenta del consumo.
	 *
	 * @param account el número de cuenta del consumo
	 */
	public void setAccount(Integer account) {
		this.account = account;
	}

	/**
	 * Devuelve la mesa en la que se atendio la cuenta.
	 *
	 * @return la mesa en la que se atendio la cuenta
	 */
	public String getTable() {
		return table;
	}

	/**
	 * Establece la mesa en la que se atendio la cuenta.
	 *
	 * @param table la mesa en la que se atendio la cuenta
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * Devuelve el nombre del mesero que atendio.
	 *
	 * @return el nombre del mesero que atendio
	 */
	public String getWaiterName() {
		return waiterName;
	}

	/**
	 * Establece el nombre del mesero que atendio.
	 *
	 * @param waiterName el nombre del mesero que atendio
	 */
	public void setWaiterName(String waiterName) {
		this.waiterName = waiterName;
	}

	/**
	 * Devuelve el indicador de si el consumo corresponde a un socio.
	 *
	 * @return el indicador de si el consumo corresponde a un socio
	 */
	public Character getIsPartner() {
		return isPartner;
	}

	/**
	 * Establece el indicador de si el consumo corresponde a un socio.
	 *
	 * @param isPartner el indicador de si el consumo corresponde a un socio
	 */
	public void setIsPartner(Character isPartner) {
		this.isPartner = isPartner;
	}

	/**
	 * Devuelve el valor neto del consumo, sin impuestos ni recargos.
	 *
	 * @return el valor neto del consumo, sin impuestos ni recargos
	 */
	public Double getConsumptionValue() {
		return consumptionValue;
	}

	/**
	 * Establece el valor neto del consumo, sin impuestos ni recargos.
	 *
	 * @param consumptionValue el valor neto del consumo, sin impuestos ni recargos
	 */
	public void setConsumptionValue(Double consumptionValue) {
		this.consumptionValue = consumptionValue;
	}

	/**
	 * Devuelve el impuesto al valor agregado.
	 *
	 * @return el impuesto al valor agregado
	 */
	public Double getIva() {
		return iva;
	}

	/**
	 * Establece el impuesto al valor agregado.
	 *
	 * @param iva el impuesto al valor agregado
	 */
	public void setIva(Double iva) {
		this.iva = iva;
	}

	/**
	 * Devuelve el recargo por servicio.
	 *
	 * @return el recargo por servicio
	 */
	public Double getService() {
		return service;
	}

	/**
	 * Establece el recargo por servicio.
	 *
	 * @param service el recargo por servicio
	 */
	public void setService(Double service) {
		this.service = service;
	}

	/**
	 * Devuelve la propina.
	 *
	 * @return la propina
	 */
	public Double getTip() {
		return tip;
	}

	/**
	 * Establece la propina.
	 *
	 * @param tip la propina
	 */
	public void setTip(Double tip) {
		this.tip = tip;
	}

	/**
	 * Devuelve el momento de apertura del consumo.
	 *
	 * @return el momento de apertura del consumo
	 */
	public LocalDateTime getConsumptionOpening() {
		return consumptionOpening;
	}

	/**
	 * Establece el momento de apertura del consumo.
	 *
	 * @param consumptionOpening el momento de apertura del consumo
	 */
	public void setConsumptionOpening(LocalDateTime consumptionOpening) {
		this.consumptionOpening = consumptionOpening;
	}

	/**
	 * Devuelve el momento de cierre del consumo.
	 *
	 * @return el momento de cierre del consumo
	 */
	public LocalDateTime getConsumptionClosing() {
		return consumptionClosing;
	}

	/**
	 * Establece el momento de cierre del consumo.
	 *
	 * @param consumptionClosing el momento de cierre del consumo
	 */
	public void setConsumptionClosing(LocalDateTime consumptionClosing) {
		this.consumptionClosing = consumptionClosing;
	}

	/**
	 * Devuelve las líneas de detalle del consumo.
	 *
	 * @return las líneas de detalle del consumo
	 */
	public List<ConsumptionItemRequest> getItems() {
		return items;
	}

	/**
	 * Establece las líneas de detalle del consumo.
	 *
	 * @param items las líneas de detalle del consumo
	 */
	public void setItems(List<ConsumptionItemRequest> items) {
		this.items = items;
	}
}
