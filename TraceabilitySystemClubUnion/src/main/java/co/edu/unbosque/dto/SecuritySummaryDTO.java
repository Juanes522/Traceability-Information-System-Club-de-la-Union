package co.edu.unbosque.dto;

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
