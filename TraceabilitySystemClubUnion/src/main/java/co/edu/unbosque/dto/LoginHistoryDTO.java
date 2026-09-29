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

	public LoginHistoryDTO() {
	}

	public LoginHistoryDTO(Instant timestamp, String ip) {
		this.timestamp = timestamp;
		this.ip = ip;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
