package co.unbosque.dto;

public class AuthResponse {
    private String token;
    private String role;
    private Boolean needsPasswordChange;

    public AuthResponse(String token, String role, Boolean needsPasswordChange) {
        this.token = token;
        this.role = role;
        this.needsPasswordChange = needsPasswordChange;
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
}
