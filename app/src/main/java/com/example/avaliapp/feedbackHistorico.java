package com.example.avaliapp;

public class feedbackHistorico {
    private String questionText; // Nome do usuário que respondeu
    private String feedbackText; // Resposta

    public feedbackHistorico(String questionText, String feedbackText) {
        this.questionText = questionText;
        this.feedbackText = feedbackText;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getFeedbackText() {
        return feedbackText;
    }
}
