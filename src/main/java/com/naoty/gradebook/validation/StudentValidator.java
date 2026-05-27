package com.naoty.gradebook.validation;

import com.naoty.gradebook.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentValidator implements Validator<Student> {

    @Override
    public ValidationResult validate(Student s) {
        List<String> errors = new ArrayList<>();
        if (s == null) return ValidationResult.fail("student.null");
        if (isBlank(s.id()))      errors.add("student.id.required");
        if (isBlank(s.nom()))     errors.add("student.nom.required");
        if (isBlank(s.prenom()))  errors.add("student.prenom.required");
        if (s.sexe() == null)      errors.add("student.sexe.required");
        if (s.niveau() == null)    errors.add("student.niveau.required");
        if (s.formation() == null) errors.add("student.formation.required");
        if (s.status() == null)    errors.add("student.status.required");
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
