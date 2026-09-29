package co.edu.unbosque.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesión.
 *
 * <p>El nombre de usuario del sistema es la <strong>identificación</strong> del socio —su cédula—, no un correo: esa
 * decisión es la que obliga a cifrar esa columna de forma determinista para poder consultarla.
 *
 * <p>Deliberadamente <strong>no</strong> valida la robustez de la contraseña. Hacerlo dejaría fuera del sistema a los
 * socios cuya contraseña heredada no cumple la política vigente, antes de darles ocasión de cambiarla.
 */
public class AuthRequest {
    @NotBlank
    private String identification;
    @NotBlank
    private String password;

    public AuthRequest() {}

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
