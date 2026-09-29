package co.edu.unbosque.dto;

/**
 * Número de intentos fallidos de autenticación agrupados por usuario.
 *
 * <p>Responde a la pregunta operativa de qué cuentas están siendo atacadas. Los eventos sin usuario identificable se
 * agrupan bajo una etiqueta genérica en lugar de descartarse.
 *
 * <p>Solo lo consume el reporte de seguridad; ningún endpoint lo devuelve.
 */
public class UserFailedCountDTO {

	private String username;
	private long count;

	public UserFailedCountDTO() {
	}

	public UserFailedCountDTO(String username, long count) {
		this.username = username;
		this.count = count;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String v) {
		this.username = v;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long v) {
		this.count = v;
	}
}
