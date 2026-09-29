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

    public AuthResponse(String token, String role, Boolean needsPasswordChange, Boolean needsConsent) {
        this.token = token;
        this.role = role;
        this.needsPasswordChange = needsPasswordChange;
        this.needsConsent = needsConsent;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getNeedsPasswordChange() {
        return needsPasswordChange;
    }

    public void setNeedsPasswordChange(Boolean needsPasswordChange) {
        this.needsPasswordChange = needsPasswordChange;
    }

    public Boolean getNeedsConsent() {
        return needsConsent;
    }

    public void setNeedsConsent(Boolean needsConsent) {
        this.needsConsent = needsConsent;
    }
}
