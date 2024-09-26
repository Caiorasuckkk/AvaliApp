package com.example.avaliapp;

public class User {
    private String id;          // ID do usuário
    private String fullName;    // Nome completo do usuário
    private String area;        // Área do usuário
    private String email;       // Email do usuário
    private String cargo;       // Cargo do usuário
    private String nivel;       // Nível do usuário
    // Outros atributos...

    // Construtor vazio necessário para o Firebase
    public User() {
    }

    // Construtor com parâmetros
    public User(String id, String fullName, String area, String email, String cargo, String nivel) {
        this.id = id;
        this.fullName = fullName;
        this.area = area;
        this.email = email;
        this.cargo = cargo;
        this.nivel = nivel;
        // Inicialização de outros atributos...
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getArea() {
        return area;
    }

    public String getEmail() {
        return email;
    }

    public String getCargo() {
        return cargo;
    }

    public String getNivel() {
        return nivel;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    // Outros métodos, se necessário...
}
