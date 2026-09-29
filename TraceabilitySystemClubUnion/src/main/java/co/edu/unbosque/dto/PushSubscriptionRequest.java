package co.edu.unbosque.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Suscripción Web Push que envía el navegador al darse de alta.
 *
 * <p>Refleja aplanado el objeto que produce la API Push del navegador: la dirección de entrega y las dos claves
 * criptográficas necesarias para cifrar el mensaje.
 *
 * <p>Nótese que el endpoint de baja acepta este mismo cuerpo <strong>sin validarlo</strong>.
 */
public class PushSubscriptionRequest {

	@NotBlank
	private String endpoint;
	@NotBlank
	private String p256dhKey;
	@NotBlank
	private String authKey;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public PushSubscriptionRequest() {
	}

	/**
	 * Devuelve la dirección de entrega de la suscripción push.
	 *
	 * @return la dirección de entrega de la suscripción push
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Establece la dirección de entrega de la suscripción push.
	 *
	 * @param endpoint la dirección de entrega de la suscripción push
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Devuelve la clave pública del cliente para la suscripción push.
	 *
	 * @return la clave pública del cliente para la suscripción push
	 */
	public String getP256dhKey() {
		return p256dhKey;
	}

	/**
	 * Establece la clave pública del cliente para la suscripción push.
	 *
	 * @param p256dhKey la clave pública del cliente para la suscripción push
	 */
	public void setP256dhKey(String p256dhKey) {
		this.p256dhKey = p256dhKey;
	}

	/**
	 * Devuelve el secreto de autenticación de la suscripción push.
	 *
	 * @return el secreto de autenticación de la suscripción push
	 */
	public String getAuthKey() {
		return authKey;
	}

	/**
	 * Establece el secreto de autenticación de la suscripción push.
	 *
	 * @param authKey el secreto de autenticación de la suscripción push
	 */
	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}
}
