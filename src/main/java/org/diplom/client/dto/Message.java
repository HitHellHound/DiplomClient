package org.diplom.client.dto;

public class Message {
    private Integer code;
    private String message;

    public Message() {

    }

    public Message(String message) {
        this.message = message;
    }

    public Message(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
