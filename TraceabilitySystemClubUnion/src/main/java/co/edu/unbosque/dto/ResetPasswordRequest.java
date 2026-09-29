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

	public ResetPasswordRequest() {
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
