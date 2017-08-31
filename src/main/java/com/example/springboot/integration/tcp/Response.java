package com.example.springboot.integration.tcp;

/**
 * Created by hocklo on 31/08/17.
 */
public class Response {
    private String message;

    public Response(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
