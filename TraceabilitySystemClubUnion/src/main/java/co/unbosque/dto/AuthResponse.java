package co.unbosque.dto;

public class AuthResponse {
    private String token;
    private Boolean needsPasswordChange;

    public AuthResponse(String token, Boolean needsPasswordChange) {
        this.token = token;
        this.needsPasswordChange = needsPasswordChange;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getNeedsPasswordChange() {
        return needsPasswordChange;
    }

    public void setNeedsPasswordChange(Boolean needsPasswordChange) {
        this.needsPasswordChange = needsPasswordChange;
    }
}
