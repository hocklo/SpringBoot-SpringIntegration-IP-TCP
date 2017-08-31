package com.example.springboot.integration.tcp;

/**
 * Created by hocklo on 31/08/17.
 */
public class Request {
    private String endpoint;
    private String message;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
