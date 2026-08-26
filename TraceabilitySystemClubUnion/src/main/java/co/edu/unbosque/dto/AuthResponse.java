package co.edu.unbosque.dto;

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
