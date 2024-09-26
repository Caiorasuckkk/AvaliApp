package com.example.avaliapp;

public class FeedbackResponse {
    private String response;
    private String responderName;
    private String createdAt;

    // Construtor
    public FeedbackResponse(String response, String responderName, String createdAt) {
        this.response = response;
        this.responderName = responderName;
        this.createdAt = createdAt;
    }

    // Construtor vazio
    public FeedbackResponse() {
    }

    // Getters e Setters
    public String getResponse() {
        return response;
    }

    public String getResponderName() {
        return responderName;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
