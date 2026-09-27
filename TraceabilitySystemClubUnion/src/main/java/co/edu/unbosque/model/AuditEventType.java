package co.edu.unbosque.model;

/**
 * Catálogo de tipos de evento de auditoría.
 *
 * <p><strong>No es una enumeración de Java</strong>, sino una clase final con constantes
 * {@code String} y constructor privado. La consecuencia es que
 * {@link AuditEvent#getEventType()} es una cadena sin verificación en tiempo de compilación y sin
 * comprobación de exhaustividad: nada impide indexar un tipo que no figure aquí.
 *
 * <p>El frontend duplica esta lista a mano en su pantalla de auditoría, sin ningún mecanismo de
 * sincronización, de modo que añadir un tipo aquí no lo hace filtrable en la interfaz.
 *
 * <h2>Cobertura</h2>
 *
 * <p>Los once tipos cubren autenticación, gestión de contraseñas, consentimiento, denegación de
 * autorización y registro de cargos. Quedan <strong>fuera</strong> dos operaciones sensibles:
 *
 * <ul>
 *   <li>El acceso <em>no autenticado</em> a un recurso protegido: {@code SecurityConfig} registra un
 *       manejador de acceso denegado pero no un {@code AuthenticationEntryPoint}, así que solo se
 *       auditan los fallos de autorización, no los de autenticación.</li>
 *   <li>La sincronización con el maestro externo de socios, que es una mutación masiva de datos
 *       personales y no deja rastro alguno en la bitácora.</li>
 * </ul>
 *
 * @see AuditSeverity
 */
public final class AuditEventType {

	public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
	public static final String LOGIN_FAILED = "LOGIN_FAILED";
	public static final String RATE_LIMIT_BLOCK = "RATE_LIMIT_BLOCK";
	public static final String LOGOUT = "LOGOUT";
	public static final String TOKEN_REVOKED = "TOKEN_REVOKED";
	public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
	public static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
	public static final String PASSWORD_RESET = "PASSWORD_RESET";
	public static final String ACCESS_DENIED = "ACCESS_DENIED";
	public static final String CHARGE_REGISTERED = "CHARGE_REGISTERED";
	public static final String CONSENT_ACCEPTED = "CONSENT_ACCEPTED";

	private AuditEventType() {
	}
}
