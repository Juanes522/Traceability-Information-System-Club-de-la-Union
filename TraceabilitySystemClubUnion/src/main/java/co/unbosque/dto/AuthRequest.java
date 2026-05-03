package co.unbosque.dto;

public class AuthRequest {
    private String identification;
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
