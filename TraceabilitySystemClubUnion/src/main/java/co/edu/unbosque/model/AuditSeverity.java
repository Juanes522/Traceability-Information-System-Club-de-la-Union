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

	public static final String INFO = "INFO";
	public static final String WARNING = "WARNING";
	public static final String CRITICAL = "CRITICAL";

	private AuditSeverity() {
	}
}
