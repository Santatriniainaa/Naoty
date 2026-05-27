package com.naoty.gradebook.validation;

import com.naoty.gradebook.model.Grade;

import java.util.ArrayList;
import java.util.List;

public class GradeValidator implements Validator<Grade> {

    @Override
    public ValidationResult validate(Grade g) {
        List<String> errors = new ArrayList<>();
        if (g == null) return ValidationResult.fail("grade.null");
        if (isBlank(g.id()))        errors.add("grade.id.required");
        if (isBlank(g.studentId())) errors.add("grade.studentId.required");
        if (isBlank(g.subjectId())) errors.add("grade.subjectId.required");
        if (isBlank(g.profId()))    errors.add("grade.profId.required");
        if (g.type() == null)       errors.add("grade.type.required");
        if (g.value() < 0 || g.value() > 20) errors.add("grade.value.range");
        if (g.date() == null)       errors.add("grade.date.required");
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
