package co.edu.unbosque.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de recuperación de contraseña, identificada por el correo del socio.
 *
 * <p>Es el único punto del sistema donde se busca a un socio por correo, y esa búsqueda no puede resolverse con una
 * consulta: el campo está cifrado con vector aleatorio, de modo que obliga a recorrer la tabla descifrando fila por fila.
 */
public class ForgotPasswordRequest {
	@NotBlank
	@Email
	private String email;

	public ForgotPasswordRequest() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
