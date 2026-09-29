package co.edu.unbosque.dto;

/**
 * Estado inicial de la sesión que se devuelve tras un inicio de sesión exitoso.
 *
 * <p>Además del token, transporta los dos indicadores que el cliente necesita para decidir si debe imponer un
 * interstitial bloqueante antes de permitir el uso de la aplicación: si el socio debe cambiar su contraseña y si debe
 * aceptar la versión vigente de la política de datos. El cliente los atiende en ese orden de precedencia.
 *
 * <p>El rol que viaja aquí es el <strong>normalizado</strong>, siempre con prefijo, no el valor crudo de la columna.
 *
 * <p>Nótese que los dos indicadores son objetos y admiten nulo, mientras que el modelo del cliente los declara como
 * booleanos obligatorios; el cliente se defiende asumiendo falso ante un nulo.
 */
public class AuthResponse {
    private String token;
    private String role;
    private Boolean needsPasswordChange;
    private Boolean needsConsent;

    /**
     * Crea una instancia con sus valores.
     *
     * @param token el token
     * @param role el rol del usuario
     * @param needsPasswordChange si el socio debe cambiar su contraseña
     * @param needsConsent si el socio debe aceptar la versión vigente de la política de datos
     */
    public AuthResponse(String token, String role, Boolean needsPasswordChange, Boolean needsConsent) {
        this.token = token;
        this.role = role;
        this.needsPasswordChange = needsPasswordChange;
        this.needsConsent = needsConsent;
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
     * Devuelve el rol del usuario.
     *
     * @return el rol del usuario
     */
    public String getRole() {
        return role;
    }

    /**
     * Establece el rol del usuario.
     *
     * @param role el rol del usuario
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Devuelve si el socio debe cambiar su contraseña.
     *
     * @return si el socio debe cambiar su contraseña
     */
    public Boolean getNeedsPasswordChange() {
        return needsPasswordChange;
    }

    /**
     * Establece si el socio debe cambiar su contraseña.
     *
     * @param needsPasswordChange si el socio debe cambiar su contraseña
     */
    public void setNeedsPasswordChange(Boolean needsPasswordChange) {
        this.needsPasswordChange = needsPasswordChange;
    }

    /**
     * Devuelve si el socio debe aceptar la versión vigente de la política de datos.
     *
     * @return si el socio debe aceptar la versión vigente de la política de datos
     */
    public Boolean getNeedsConsent() {
        return needsConsent;
    }

    /**
     * Establece si el socio debe aceptar la versión vigente de la política de datos.
     *
     * @param needsConsent si el socio debe aceptar la versión vigente de la política de datos
     */
    public void setNeedsConsent(Boolean needsConsent) {
        this.needsConsent = needsConsent;
    }
}
