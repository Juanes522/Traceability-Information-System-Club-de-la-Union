package co.edu.unbosque.dto;

import co.edu.unbosque.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

/**
 * Establecimiento de una contraseña nueva mediante un token de recuperación.
 *
 * <p>El token es la <strong>única credencial</strong> de esta operación: no hay sesión. De ahí que sea un valor
 * impredecible, de un solo uso y con vigencia limitada a una hora.
 */
public class ResetPasswordRequest {
	@NotBlank
	private String token;
	@NotBlank
	@StrongPassword
	private String newPassword;

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ResetPasswordRequest() {
	}

	/**
	 * Devuelve el token.
	 *
	 * @return el token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Establece el token.
	 *
	 * @param token el token
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Devuelve la contraseña nueva.
	 *
	 * @return la contraseña nueva
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * Establece la contraseña nueva.
	 *
	 * @param newPassword la contraseña nueva
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
