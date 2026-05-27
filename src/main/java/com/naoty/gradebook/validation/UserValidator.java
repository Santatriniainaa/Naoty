package com.naoty.gradebook.validation;

import com.naoty.gradebook.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserValidator implements Validator<User> {

    @Override
    public ValidationResult validate(User u) {
        List<String> errors = new ArrayList<>();
        if (u == null) return ValidationResult.fail("user.null");
        if (isBlank(u.id()))     errors.add("user.id.required");
        if (u.role() == null)    errors.add("user.role.required");
        if (isBlank(u.nom()))    errors.add("user.nom.required");
        if (isBlank(u.prenom())) errors.add("user.prenom.required");
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
