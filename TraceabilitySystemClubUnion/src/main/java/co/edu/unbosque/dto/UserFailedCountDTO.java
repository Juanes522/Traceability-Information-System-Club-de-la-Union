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

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public UserFailedCountDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param username el sujeto del evento, que es la cédula del socio sin enmascarar
	 * @param count el número de elementos agregados
	 */
	public UserFailedCountDTO(String username, long count) {
		this.username = username;
		this.count = count;
	}

	/**
	 * Devuelve el sujeto del evento, que es la cédula del socio sin enmascarar.
	 *
	 * @return el sujeto del evento, que es la cédula del socio sin enmascarar
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Establece el sujeto del evento, que es la cédula del socio sin enmascarar.
	 *
	 * @param v el sujeto del evento, que es la cédula del socio sin enmascarar
	 */
	public void setUsername(String v) {
		this.username = v;
	}

	/**
	 * Devuelve el número de elementos agregados.
	 *
	 * @return el número de elementos agregados
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Establece el número de elementos agregados.
	 *
	 * @param v el número de elementos agregados
	 */
	public void setCount(long v) {
		this.count = v;
	}
}
