package co.edu.unbosque.dto;

import java.time.Instant;

/**
 * Entrada del historial de accesos al sistema que cada socio puede consultar sobre sí mismo.
 *
 * <p>Es una proyección reducida de la bitácora de auditoría: de todo el evento solo se exponen la fecha y la dirección de
 * origen, lo justo para que el socio detecte accesos que no reconoce.
 *
 * <p>Conviene saber que la dirección registrada proviene de una cabecera que el cliente puede falsificar, de modo que no
 * debe tratarse como prueba de origen.
 */
public class LoginHistoryDTO {
	private Instant timestamp;
	private String ip;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public LoginHistoryDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param timestamp el instante del evento
	 * @param ip la dirección de origen registrada
	 */
	public LoginHistoryDTO(Instant timestamp, String ip) {
		this.timestamp = timestamp;
		this.ip = ip;
	}

	/**
	 * Devuelve el instante del evento.
	 *
	 * @return el instante del evento
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Establece el instante del evento.
	 *
	 * @param timestamp el instante del evento
	 */
	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Devuelve la dirección de origen registrada.
	 *
	 * @return la dirección de origen registrada
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Establece la dirección de origen registrada.
	 *
	 * @param ip la dirección de origen registrada
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
}
