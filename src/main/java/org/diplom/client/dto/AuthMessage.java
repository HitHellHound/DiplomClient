package org.diplom.client.dto;

public class AuthMessage extends Message {
    private String identifier;
    private String password;

    public AuthMessage() {
    }

    public AuthMessage(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public AuthMessage(Integer code, String message, String identifier, String password) {
        super(code, message);
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
