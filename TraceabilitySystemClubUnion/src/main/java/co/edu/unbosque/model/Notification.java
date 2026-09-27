package co.edu.unbosque.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Aviso persistente generado al registrarse un consumo.
 *
 * <p>Es el registro duradero de la notificación: el correo y el mensaje Web Push son entregas
 * efímeras y pueden fallar en silencio, mientras que esta fila es lo que el socio consulta después
 * en {@code GET /personpartner/notifications/me}.
 *
 * <p>El aviso es <strong>de un solo sentido</strong>. El socio lo lee, pero no existe forma de
 * confirmarlo ni de objetarlo: no hay entidad de validación de consumo ni endpoint que la respalde.
 *
 * <h2>La propiedad del aviso es indirecta</h2>
 *
 * <p>No hay relación con {@link PersonPartner}. Saber de quién es un aviso exige recorrer
 * {@code notification → consumption → partner}, que es exactamente lo que hace la consulta derivada
 * {@code findByConsumptionPartnerIdentificationOrderByGenerationDateDesc}. Ese recorrido de tres
 * niveles funciona sobre una columna cifrada porque el parámetro atraviesa el conversor determinista
 * antes de llegar al {@code WHERE}.
 *
 * @see co.edu.unbosque.dto.NotificationDTO
 */
@Entity
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long notificationId;

	/**
	 * Tipo de aviso.
	 *
	 * <p>El único valor que escribe el código es el literal {@code "CHARGE_NOTIFICATION"}, en
	 * {@link co.edu.unbosque.service.PartnerConsumptionService}. El campo es {@code String} y no
	 * existe enumeración que lo restrinja, de modo que la estructura admite otros tipos de aviso,
	 * pero ninguno está implementado.
	 */
	private String notificationType;
	private String title;
	private String body;
	/**
	 * Momento del aviso.
	 *
	 * <p>Se fija a la <strong>hora de apertura del consumo</strong>, no al instante de creación del
	 * aviso. Es también el criterio de ordenación del listado que ve el socio.
	 */
	private LocalDateTime generationDate;
	/**
	 * Estado del aviso.
	 *
	 * <p><strong>El único valor que el código escribe es {@code 'S'}</strong>, en
	 * {@link co.edu.unbosque.service.PartnerConsumptionService}. No hay enumeración, ni máquina de
	 * estados, ni transición alguna en ninguna parte del sistema.
	 *
	 * <p>Qué significa {@code 'S'} y qué otros estados se pretendían <strong>no puede determinarse
	 * leyendo el código</strong>. El frontend lo muestra sin interpretarlo, y no existe operación de
	 * «marcar como leído».
	 */
	private Character state;

	/** Consumo que originó el aviso. Lado propietario de la clave ajena, y única vía hacia el socio. */
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "consumption_id")
	private PartnerConsumption consumption;

	public Notification() {
	}

	public Long getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public LocalDateTime getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(LocalDateTime generationDate) {
		this.generationDate = generationDate;
	}

	public Character getState() {
		return state;
	}

	public void setState(Character state) {
		this.state = state;
	}

	public PartnerConsumption getConsumption() {
		return consumption;
	}

	public void setConsumption(PartnerConsumption consumption) {
		this.consumption = consumption;
	}
}
