package co.edu.unbosque.model;

/**
 * Severidad de un evento de auditoría.
 *
 * <p>No la asigna quien registra el evento: la <strong>deriva</strong>
 * {@link co.edu.unbosque.service.AuditService} a partir del tipo y del desenlace, lo que garantiza
 * una clasificación uniforme en todo el sistema:
 *
 * <ol>
 *   <li>{@code RATE_LIMIT_BLOCK} o {@code ACCESS_DENIED} → {@link #CRITICAL}</li>
 *   <li>cualquier otro evento con resultado {@code FAILURE} → {@link #WARNING}</li>
 *   <li>el resto → {@link #INFO}</li>
 * </ol>
 *
 * <p>{@link #CRITICAL} es el valor que cuenta {@code SecurityMetricsService} para el indicador de
 * alertas del panel de administración, y el que selecciona los eventos del reporte PDF de seguridad.
 *
 * <p>Como {@link AuditEventType}, es una clase de constantes y no una enumeración.
 */
public final class AuditSeverity {

	/**
	 * Evento informativo, sin implicación de seguridad.
	 */
	public static final String INFO = "INFO";
	/**
	 * Evento que merece atención. Se asigna a cualquier evento con desenlace de fallo.
	 */
	public static final String WARNING = "WARNING";
	/**
	 * Evento de seguridad grave. Alimenta el contador de alertas del panel de administración.
	 */
	public static final String CRITICAL = "CRITICAL";

	private AuditSeverity() {
	}
}
