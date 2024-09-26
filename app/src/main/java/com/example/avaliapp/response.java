package com.example.avaliapp;

public class response {
    private String responderName;
    private String response;

    public response(String responderName, String response) {
        this.responderName = responderName;
        this.response = response;
    }

    public String getResponderName() {
        return responderName;
    }

    public String getResponse() {
        return response;
    }
}
