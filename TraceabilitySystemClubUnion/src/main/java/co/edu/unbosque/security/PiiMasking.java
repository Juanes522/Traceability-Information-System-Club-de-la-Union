package co.edu.unbosque.security;

/**
 * Utilidad para enmascarar datos personales antes de registrarlos.
 *
 * <p><strong>Su cobertura es mucho más estrecha de lo que el nombre sugiere.</strong> Ofrece un único
 * método, para correos, y tiene un único punto de llamada: el evento de auditoría de solicitud de
 * recuperación de contraseña.
 *
 * <p>Consecuencia directa: todos los demás eventos de la bitácora almacenan la {@code identification}
 * del socio <strong>en claro</strong> en su campo {@code username}, de modo que el índice de auditoría
 * contiene cédulas sin enmascarar. No existen {@code maskIdentification} ni {@code maskPhone}.
 *
 * <p>Para un sistema cuyo propósito declarado incluye el cumplimiento de la normativa de protección de
 * datos personales, esa asimetría es una brecha de coherencia y no un detalle menor.
 */
public final class PiiMasking {

	private PiiMasking() {
	}

	/**
	 * Enmascara una dirección de correo conservando su primera letra y su dominio.
	 *
	 * <p>Por ejemplo, {@code juan@club.com} produce {@code j***@club.com}.
	 *
	 * <p>El enmascaramiento es <strong>deliberadamente parcial</strong>: preservar el dominio permite
	 * diagnosticar problemas de entrega leyendo la bitácora, a costa de no ser una anonimización real.
	 * Conserva además la primera letra, de modo que frente a un conjunto reducido de destinatarios
	 * conocidos puede seguir siendo identificable.
	 *
	 * @param email dirección a enmascarar
	 * @return la dirección enmascarada; el valor original si es {@code null} o está en blanco, o
	 *         {@code "***"} si no contiene una arroba en posición válida
	 */
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
