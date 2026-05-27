package com.naoty.gradebook.model;

public record Student(
        String id,
        String nom,
        String prenom,
        Gender sexe,
        Niveau niveau,
        Formation formation,
        Status status
) {
    public Student {
        if (formation == Formation.BOTH) {
            throw new IllegalArgumentException("Un étudiant ne peut pas avoir la formation BOTH");
        }
    }

    public String fullName() {
        return prenom + " " + nom;
    }
}
