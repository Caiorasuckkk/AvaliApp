package com.example.avaliapp;

public class UserReuni {

    private String fullName;
    private String email;
    private String cargo;
    private String area;
    private boolean isSelected; // Para marcar se o usuário foi selecionado para a reunião

    public UserReuni(String fullName, String email,String area) {
        this.fullName = fullName;
        this.email = email;
        this.cargo = cargo;
        this.area = area;
        this.isSelected = false;
    }

    // Getters e Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
