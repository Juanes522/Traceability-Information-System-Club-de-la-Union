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

    /**
     * Constructor sin argumentos requerido para la deserialización del cuerpo de la petición.
     */
    public AuthRequest() {}

    /**
     * Devuelve la cédula del socio, que es el nombre de usuario del sistema.
     *
     * @return la cédula del socio, que es el nombre de usuario del sistema
     */
    public String getIdentification() {
        return identification;
    }

    /**
     * Establece la cédula del socio, que es el nombre de usuario del sistema.
     *
     * @param identification la cédula del socio, que es el nombre de usuario del sistema
     */
    public void setIdentification(String identification) {
        this.identification = identification;
    }

    /**
     * Devuelve la contraseña, con hash BCrypt o en texto plano si es heredada.
     *
     * @return la contraseña, con hash BCrypt o en texto plano si es heredada
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña, con hash BCrypt o en texto plano si es heredada.
     *
     * @param password la contraseña, con hash BCrypt o en texto plano si es heredada
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
