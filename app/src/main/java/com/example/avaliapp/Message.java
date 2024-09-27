package com.example.avaliapp;

public class Message {
    private String senderId;
    private String messageContent;
    private String senderFullName; // Adicionando o campo para o nome completo

    // Construtor
    public Message() {
    }

    public Message(String senderId, String messageContent, String senderFullName) {
        this.senderId = senderId;
        this.messageContent = messageContent;
        this.senderFullName = senderFullName; // Inicializando o nome completo
    }

    // Getters e Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public String getSenderFullName() { return senderFullName; }
    public void setSenderFullName(String senderFullName) { this.senderFullName = senderFullName; }
}
