package com.naoty.gradebook.model;

public record User(
        String id,
        Role role,
        String nom,
        String prenom
) {
    public String fullName() {
        return prenom + " " + nom;
    }
}
