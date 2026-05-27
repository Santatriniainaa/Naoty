package com.naoty.gradebook.model;

/**
 * Semestre universitaire absolu, de S1 (premier semestre de L1)
 * à S10 (second semestre de M2).
 */
public enum Semestre {
    S1, S2, S3, S4, S5, S6, S7, S8, S9, S10;

    /** Niveau auquel ce semestre appartient. */
    public Niveau niveau() {
        int n = ordinal() / 2;
        return Niveau.values()[n];
    }

    /** Tous les semestres d'un niveau donné. */
    public static Semestre[] of(Niveau niveau) {
        int base = niveau.ordinal() * 2;
        return new Semestre[]{ values()[base], values()[base + 1] };
    }
}
