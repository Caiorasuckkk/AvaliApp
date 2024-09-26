package com.example.avaliapp;

import java.util.List;
import java.util.ArrayList;

public class Group {
    private String id;
    private String title;
    private String creatorId;
    private List<String> users; // Lista de IDs de usuários
    private List<String> userNames; // Lista de nomes de usuários
    private String creatorFullName; // Nome completo do criador

    // Construtor
    public Group() {
        users = new ArrayList<>();
        userNames = new ArrayList<>();
    }

    // Getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public List<String> getUsers() { return users; }
    public void setUsers(List<String> users) { this.users = users; }

    public List<String> getUserNames() { return userNames; }
    public void setUserNames(List<String> userNames) { this.userNames = userNames; }

    public String getCreatorFullName() { return creatorFullName; }
    public void setCreatorFullName(String creatorFullName) { this.creatorFullName = creatorFullName; }
}
