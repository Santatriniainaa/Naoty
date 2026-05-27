package com.naoty.gradebook.model;

public enum Formation {
    ACADEMIQUE, PROFESSIONNEL, BOTH;

    /**
     * Une matière de formation BOTH est ouverte aux deux. Sinon, exact match.
     * Un étudiant ne peut être ACADEMIQUE ou PROFESSIONNEL (jamais BOTH).
     */
    public boolean matches(Formation studentFormation) {
        if (this == BOTH) return true;
        return this == studentFormation;
    }
}
