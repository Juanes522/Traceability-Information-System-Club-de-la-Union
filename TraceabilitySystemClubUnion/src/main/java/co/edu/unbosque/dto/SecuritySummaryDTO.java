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

	public long getLoginFailedCount() {
		return loginFailedCount;
	}

	public void setLoginFailedCount(long v) {
		this.loginFailedCount = v;
	}

	public long getRateLimitBlockCount() {
		return rateLimitBlockCount;
	}

	public void setRateLimitBlockCount(long v) {
		this.rateLimitBlockCount = v;
	}

	public long getAccessDeniedCount() {
		return accessDeniedCount;
	}

	public void setAccessDeniedCount(long v) {
		this.accessDeniedCount = v;
	}

	public long getCriticalAlertCount() {
		return criticalAlertCount;
	}

	public void setCriticalAlertCount(long v) {
		this.criticalAlertCount = v;
	}

	private boolean degraded;

	public boolean isDegraded() {
		return degraded;
	}

	public void setDegraded(boolean degraded) {
		this.degraded = degraded;
	}
}
