package com.naoty.gradebook.model;

import java.time.LocalDate;

/**
 * Une note ponctuelle saisie par un prof. Le semestre et le niveau ne sont pas
 * stockés ici : ils sont dérivés de la matière liée ({@code subjectId}).
 */
public record Grade(
        String id,
        String studentId,
        String subjectId,
        String profId,
        GradeType type,
        double value,
        LocalDate date
) {
    public Grade {
        if (value < 0 || value > 20) {
            throw new IllegalArgumentException("Note hors 0-20 : " + value);
        }
    }
}
