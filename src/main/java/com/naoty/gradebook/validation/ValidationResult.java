package com.naoty.gradebook.validation;

import java.util.ArrayList;
import java.util.List;

public record ValidationResult(boolean valid, List<String> errors) {

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(String error) {
        return new ValidationResult(false, List.of(error));
    }

    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, List.copyOf(errors));
    }

    public ValidationResult merge(ValidationResult other) {
        if (this.valid && other.valid) return ok();
        List<String> all = new ArrayList<>(this.errors);
        all.addAll(other.errors);
        return new ValidationResult(false, List.copyOf(all));
    }
}
