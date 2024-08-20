package org.l2jbr_unity.gameserver.api.responses;

public class LoginResponse extends Response {
    private final String token;

    public LoginResponse(int code, String message, String token) {
        super(code, message);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
