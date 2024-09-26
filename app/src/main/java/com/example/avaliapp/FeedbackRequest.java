package com.example.avaliapp;

import java.util.HashMap;
import java.util.Map;

public class FeedbackRequest {
    private String requestId; // ID da solicitação
    private String requesterId; // ID do solicitante (usuário)
    private String requesterFullName; // Nome completo do solicitante
    private String feedback; // Feedback do usuário
    private String createdAt; // Data de criação
    private Map<String, FeedbackResponse> responses; // Respostas recebidas

    // Construtor padrão (sem argumentos)
    public FeedbackRequest() {
        this.requestId = "";
        this.requesterId = ""; // Inicializa com valor vazio
        this.requesterFullName = ""; // Inicializa com valor vazio
        this.feedback = "";
        this.createdAt = "";
        this.responses = new HashMap<>(); // Inicializa o mapa de respostas
    }

    // Construtor com parâmetros
    public FeedbackRequest(String requestId, String requesterId, String requesterFullName, String feedback, String createdAt) {
        this.requestId = requestId;
        this.requesterId = requesterId; // Armazena o ID do solicitante
        this.requesterFullName = requesterFullName; // Armazena o nome completo do solicitante
        this.feedback = feedback;
        this.createdAt = createdAt;
        this.responses = new HashMap<>(); // Inicializa o mapa de respostas
    }

    // Getters e Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public void setRequesterFullName(String requesterFullName) {
        this.requesterFullName = requesterFullName;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, FeedbackResponse> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, FeedbackResponse> responses) {
        this.responses = responses;
    }

    // Método para adicionar uma resposta
    public void addResponse(String questionId, FeedbackResponse response) {
        responses.put(questionId, response);
    }
}
