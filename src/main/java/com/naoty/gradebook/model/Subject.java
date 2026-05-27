package com.naoty.gradebook.model;

/**
 * Matière universitaire. Chaque matière est rattachée à un niveau,
 * un semestre, une formation (éventuellement BOTH), et un prof.
 *
 * <p>Les poids DS/TP/EXAM permettent de pondérer les notes : leur somme
 * doit être strictement positive ; les notes manquantes sont ignorées
 * et les poids restants se re-normalisent automatiquement (côté StatsService).
 */
public record Subject(
        String id,
        String nom,
        int coefficient,
        Niveau niveau,
        Semestre semestre,
        Formation formation,
        String profId,
        double weightDS,
        double weightTP,
        double weightExam
) {
    public Subject {
        if (coefficient < 1 || coefficient > 10) {
            throw new IllegalArgumentException("Coefficient hors 1-10 : " + coefficient);
        }
        if (semestre.niveau() != niveau) {
            throw new IllegalArgumentException(
                    "Incohérence : semestre " + semestre + " n'appartient pas au niveau " + niveau);
        }
        if (weightDS < 0 || weightTP < 0 || weightExam < 0) {
            throw new IllegalArgumentException("Poids négatif interdit");
        }
        if (weightDS + weightTP + weightExam <= 0) {
            throw new IllegalArgumentException("Somme des poids doit être strictement positive");
        }
    }

    public double weight(GradeType type) {
        return switch (type) {
            case DS   -> weightDS;
            case TP   -> weightTP;
            case EXAM -> weightExam;
        };
    }
}
