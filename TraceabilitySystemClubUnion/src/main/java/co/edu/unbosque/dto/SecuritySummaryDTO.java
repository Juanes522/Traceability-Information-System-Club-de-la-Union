package co.edu.unbosque.dto;

/**
 * Contadores del panel de seguridad, calculados sobre la bitácora de auditoría.
 *
 * <p>El campo que lo distingue del resto de los DTO de métricas es el indicador de <strong>degradación</strong>: cuando la
 * bitácora no está disponible, el endpoint responde correctamente con contadores parciales y activa ese indicador en lugar
 * de fallar.
 *
 * <p>El cliente <strong>debe</strong> mostrarlo. Unos contadores en cero podrían leerse como ausencia de incidentes cuando
 * en realidad significan ausencia de datos, que es una conclusión muy distinta en un panel de seguridad.
 */
public class SecuritySummaryDTO {

	private long loginFailedCount;
	private long rateLimitBlockCount;
	private long accessDeniedCount;
	private long criticalAlertCount;

	/**
	 * Devuelve el número de inicios de sesión fallidos del periodo.
	 *
	 * @return el número de inicios de sesión fallidos del periodo
	 */
	public long getLoginFailedCount() {
		return loginFailedCount;
	}

	/**
	 * Establece el número de inicios de sesión fallidos del periodo.
	 *
	 * @param v el número de inicios de sesión fallidos del periodo
	 */
	public void setLoginFailedCount(long v) {
		this.loginFailedCount = v;
	}

	/**
	 * Devuelve el número de bloqueos por exceso de intentos del periodo.
	 *
	 * @return el número de bloqueos por exceso de intentos del periodo
	 */
	public long getRateLimitBlockCount() {
		return rateLimitBlockCount;
	}

	/**
	 * Establece el número de bloqueos por exceso de intentos del periodo.
	 *
	 * @param v el número de bloqueos por exceso de intentos del periodo
	 */
	public void setRateLimitBlockCount(long v) {
		this.rateLimitBlockCount = v;
	}

	/**
	 * Devuelve el número de accesos denegados del periodo.
	 *
	 * @return el número de accesos denegados del periodo
	 */
	public long getAccessDeniedCount() {
		return accessDeniedCount;
	}

	/**
	 * Establece el número de accesos denegados del periodo.
	 *
	 * @param v el número de accesos denegados del periodo
	 */
	public void setAccessDeniedCount(long v) {
		this.accessDeniedCount = v;
	}

	/**
	 * Devuelve el número de alertas críticas del periodo.
	 *
	 * @return el número de alertas críticas del periodo
	 */
	public long getCriticalAlertCount() {
		return criticalAlertCount;
	}

	/**
	 * Establece el número de alertas críticas del periodo.
	 *
	 * @param v el número de alertas críticas del periodo
	 */
	public void setCriticalAlertCount(long v) {
		this.criticalAlertCount = v;
	}

	private boolean degraded;

	/**
	 * Devuelve si la bitacora no estaba disponible y los contadores no son fiables.
	 *
	 * @return si la bitacora no estaba disponible y los contadores no son fiables
	 */
	public boolean isDegraded() {
		return degraded;
	}

	/**
	 * Establece si la bitacora no estaba disponible y los contadores no son fiables.
	 *
	 * @param degraded si la bitacora no estaba disponible y los contadores no son fiables
	 */
	public void setDegraded(boolean degraded) {
		this.degraded = degraded;
	}
}
