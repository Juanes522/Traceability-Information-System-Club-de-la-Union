package co.edu.unbosque.dto;

import java.time.LocalDateTime;

/**
 * Aviso de cargo tal como lo consulta el socio.
 *
 * <p>Enriquece el aviso almacenado con dos datos del consumo asociado —el ambiente y el importe total— que la entidad de
 * notificación no guarda. El importe se <strong>recalcula</strong> en cada lectura a partir del consumo, y ese
 * enriquecimiento es lo que provoca una consulta adicional por cada fila de la página.
 *
 * <p>Nótese que aquí el ambiente se escribe correctamente, mientras que en la entidad de consumo el campo equivalente
 * conserva una errata histórica.
 */
public class NotificationDTO {

	private Long notificationId;
	private String title;
	private String body;
	private LocalDateTime generationDate;
	private Character state;
	private Long consumptionId;
	private String environment;
	private Double totalAmount;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public NotificationDTO() {
	}

	/**
	 * Crea el aviso con todos sus valores, incluidos los que se derivan del consumo asociado.
	 *
	 * @param notificationId identificador del aviso
	 * @param title          titulo del aviso
	 * @param body           cuerpo del aviso
	 * @param generationDate momento del aviso, que coincide con la apertura del consumo
	 * @param state          estado del aviso
	 * @param consumptionId  identificador del consumo que lo origino
	 * @param environment    ambiente del club donde se produjo el consumo
	 * @param totalAmount    importe total del cargo, recalculado desde el consumo
	 */
	public NotificationDTO(Long notificationId, String title, String body, LocalDateTime generationDate,
			Character state, Long consumptionId, String environment, Double totalAmount) {
		this.notificationId = notificationId;
		this.title = title;
		this.body = body;
		this.generationDate = generationDate;
		this.state = state;
		this.consumptionId = consumptionId;
		this.environment = environment;
		this.totalAmount = totalAmount;
	}

	/**
	 * Devuelve el identificador del aviso.
	 *
	 * @return el identificador del aviso
	 */
	public Long getNotificationId() {
		return notificationId;
	}

	/**
	 * Establece el identificador del aviso.
	 *
	 * @param notificationId el identificador del aviso
	 */
	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	/**
	 * Devuelve el titulo.
	 *
	 * @return el titulo
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Establece el titulo.
	 *
	 * @param title el titulo
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Devuelve el cuerpo del aviso.
	 *
	 * @return el cuerpo del aviso
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Establece el cuerpo del aviso.
	 *
	 * @param body el cuerpo del aviso
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Devuelve el momento del aviso, que coincide con la apertura del consumo.
	 *
	 * @return el momento del aviso, que coincide con la apertura del consumo
	 */
	public LocalDateTime getGenerationDate() {
		return generationDate;
	}

	/**
	 * Establece el momento del aviso, que coincide con la apertura del consumo.
	 *
	 * @param generationDate el momento del aviso, que coincide con la apertura del consumo
	 */
	public void setGenerationDate(LocalDateTime generationDate) {
		this.generationDate = generationDate;
	}

	/**
	 * Devuelve el estado del aviso.
	 *
	 * @return el estado del aviso
	 */
	public Character getState() {
		return state;
	}

	/**
	 * Establece el estado del aviso.
	 *
	 * @param state el estado del aviso
	 */
	public void setState(Character state) {
		this.state = state;
	}

	/**
	 * Devuelve el identificador del consumo.
	 *
	 * @return el identificador del consumo
	 */
	public Long getConsumptionId() {
		return consumptionId;
	}

	/**
	 * Establece el identificador del consumo.
	 *
	 * @param consumptionId el identificador del consumo
	 */
	public void setConsumptionId(Long consumptionId) {
		this.consumptionId = consumptionId;
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
	 * Devuelve el importe total del cargo, recalculado desde el consumo.
	 *
	 * @return el importe total del cargo, recalculado desde el consumo
	 */
	public Double getTotalAmount() {
		return totalAmount;
	}

	/**
	 * Establece el importe total del cargo, recalculado desde el consumo.
	 *
	 * @param totalAmount el importe total del cargo, recalculado desde el consumo
	 */
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
}
