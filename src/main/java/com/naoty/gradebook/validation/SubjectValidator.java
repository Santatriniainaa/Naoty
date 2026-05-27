package com.naoty.gradebook.validation;

import com.naoty.gradebook.model.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectValidator implements Validator<Subject> {

    @Override
    public ValidationResult validate(Subject s) {
        List<String> errors = new ArrayList<>();
        if (s == null) return ValidationResult.fail("subject.null");
        if (isBlank(s.id()))      errors.add("subject.id.required");
        if (isBlank(s.nom()))     errors.add("subject.nom.required");
        if (s.coefficient() < 1 || s.coefficient() > 10) errors.add("subject.coefficient.range");
        if (s.niveau() == null)    errors.add("subject.niveau.required");
        if (s.semestre() == null)  errors.add("subject.semestre.required");
        else if (s.niveau() != null && s.semestre().niveau() != s.niveau())
            errors.add("subject.semestre.mismatch");
        if (s.formation() == null) errors.add("subject.formation.required");
        if (isBlank(s.profId()))   errors.add("subject.profId.required");
        if (s.weightDS() + s.weightTP() + s.weightExam() <= 0)
            errors.add("subject.weights.invalid");
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
