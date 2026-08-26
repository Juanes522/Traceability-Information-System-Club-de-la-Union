package co.edu.unbosque.model;

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
