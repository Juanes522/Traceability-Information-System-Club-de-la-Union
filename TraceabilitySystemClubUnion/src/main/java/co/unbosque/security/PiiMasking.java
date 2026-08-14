package co.unbosque.security;

public final class PiiMasking {

	private PiiMasking() {
	}

	public static String maskEmail(String email) {
		if (email == null || email.isBlank()) {
			return email;
		}
		int at = email.indexOf('@');
		if (at <= 0) {
			return "***";
		}
		return email.charAt(0) + "***" + email.substring(at);
	}
}
