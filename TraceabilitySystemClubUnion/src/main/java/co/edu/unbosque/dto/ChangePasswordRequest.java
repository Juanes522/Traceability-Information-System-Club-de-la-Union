package co.edu.unbosque.dto;

import co.edu.unbosque.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de cambio de contraseña del usuario autenticado.
 *
 * <p><strong>Contiene únicamente la contraseña nueva: no pide la actual.</strong> La consecuencia es que un token válido
 * basta para tomar la cuenta de forma permanente, sin conocer la contraseña anterior.
 *
 * <p>La identidad no viaja en el cuerpo: la resuelve el controlador desde el contexto de seguridad, de modo que nadie
 * puede cambiar la contraseña de otro.
 */
public class ChangePasswordRequest {
    @NotBlank
    @StrongPassword
    private String newPassword;

    public ChangePasswordRequest() {}

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
