package co.edu.unbosque.dto;

/**
 * Resultado de una sincronización con el maestro externo de socios.
 *
 * <p><strong>No incluye contador de fallos</strong>, de modo que los errores parciales son invisibles para el
 * administrador que disparó la operación: un feed procesado a medias y uno procesado por completo producen respuestas
 * indistinguibles salvo por los números.
 */
public class SyncResultDTO {
	private long created;
	private long updated;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public SyncResultDTO() {
	}

	/**
	 * Crea una instancia con sus valores.
	 *
	 * @param created el número de socios creados
	 * @param updated el número de socios actualizados
	 */
	public SyncResultDTO(long created, long updated) {
		this.created = created;
		this.updated = updated;
	}

	/**
	 * Devuelve el número de socios creados.
	 *
	 * @return el número de socios creados
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * Establece el número de socios creados.
	 *
	 * @param created el número de socios creados
	 */
	public void setCreated(long created) {
		this.created = created;
	}

	/**
	 * Devuelve el número de socios actualizados.
	 *
	 * @return el número de socios actualizados
	 */
	public long getUpdated() {
		return updated;
	}

	/**
	 * Establece el número de socios actualizados.
	 *
	 * @param updated el número de socios actualizados
	 */
	public void setUpdated(long updated) {
		this.updated = updated;
	}
}
