package co.edu.unbosque.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Suscripción Web Push de un navegador concreto de un socio.
 *
 * <p>Refleja el objeto {@code PushSubscription} de la API Push del navegador, aplanado: la URL de
 * entrega y las dos claves criptográficas necesarias para cifrar el mensaje. Un mismo socio puede
 * tener varias filas, una por navegador o dispositivo.
 *
 * <p>La crea {@link co.edu.unbosque.controller.PushSubscriptionController}, que es el único
 * controlador del sistema que persiste directamente por repositorio sin pasar por un servicio.
 *
 * <h2>Las suscripciones no se depuran nunca</h2>
 *
 * <p>Dos razones concurrentes:
 *
 * <ul>
 *   <li>{@link co.edu.unbosque.service.PushNotificationService} no interpreta las respuestas 404 ni
 *       410 de los servicios push, de modo que un endpoint caducado se reintenta indefinidamente.</li>
 *   <li>El endpoint de baja no funciona en la práctica, y el frontend tampoco lo invoca: se da de
 *       baja solo en el navegador.</li>
 * </ul>
 *
 * <p>La tabla crece por tanto de forma monótona, y cada notificación de consumo intenta la entrega
 * contra todas las filas del socio.
 */
@Entity
@Table(name = "push_subscription")
public class PushSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * URL del servicio push del navegador a la que se entrega el mensaje. Identifica la suscripción.
	 *
	 * <p>Aunque no es un secreto criptográfico, sí es el dato que permite borrar una suscripción: el
	 * endpoint de baja lo acepta sin comprobar la propiedad.
	 *
	 * <p><strong>Consideración de esquema:</strong> la combinación de {@code length = 512} con
	 * {@code unique = true} produce en SQL Server una clave de índice de 1024 bytes, por encima del
	 * límite de 900 bytes para índices únicos. La creación de ese índice por
	 * {@code ddl-auto=update} puede fallar o quedar en advertencia.
	 */
	@Column(length = 512, nullable = false, unique = true)
	private String endpoint;

	/** Clave pública P-256 del cliente (campo {@code keys.p256dh} del navegador). */
	@Column(name = "p256dh_key", length = 256, nullable = false)
	private String p256dhKey;

	/** Secreto de autenticación del cliente (campo {@code keys.auth} del navegador). */
	@Column(name = "auth_key", length = 64, nullable = false)
	private String authKey;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	private PersonPartner partner;

	public PushSubscription() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getP256dhKey() {
		return p256dhKey;
	}

	public void setP256dhKey(String p256dhKey) {
		this.p256dhKey = p256dhKey;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public PersonPartner getPartner() {
		return partner;
	}

	public void setPartner(PersonPartner partner) {
		this.partner = partner;
	}
}
