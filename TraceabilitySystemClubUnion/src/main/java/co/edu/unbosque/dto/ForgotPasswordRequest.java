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

	/**
	 * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
	 */
	public ForgotPasswordRequest() {
	}

	/**
	 * Devuelve las direcciones de correo del socio, cifradas en reposo.
	 *
	 * @return las direcciones de correo del socio, cifradas en reposo
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Establece las direcciones de correo del socio, cifradas en reposo.
	 *
	 * @param email las direcciones de correo del socio, cifradas en reposo
	 */
	public void setEmail(String email) {
		this.email = email;
	}
}
