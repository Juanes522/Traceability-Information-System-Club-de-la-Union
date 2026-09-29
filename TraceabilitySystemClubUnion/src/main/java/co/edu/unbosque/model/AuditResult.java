package co.edu.unbosque.model;

/**
 * Desenlace de un evento de auditoría: éxito o fallo.
 *
 * <p>Como {@link AuditEventType}, es una clase de constantes y no una enumeración, de modo que el
 * campo correspondiente de {@link AuditEvent} es una cadena sin verificación en compilación.
 *
 * <p>Este valor participa en la derivación de la severidad: cualquier evento con resultado
 * {@link #FAILURE} que no sea ya crítico se clasifica como {@link AuditSeverity#WARNING}.
 */
public final class AuditResult {

	/**
	 * La operación se completó correctamente.
	 */
	public static final String SUCCESS = "SUCCESS";
	/**
	 * La operación no se completó. Eleva la severidad del evento a advertencia.
	 */
	public static final String FAILURE = "FAILURE";

	private AuditResult() {
	}
}
