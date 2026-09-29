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

	public SyncResultDTO() {
	}

	public SyncResultDTO(long created, long updated) {
		this.created = created;
		this.updated = updated;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}
}
