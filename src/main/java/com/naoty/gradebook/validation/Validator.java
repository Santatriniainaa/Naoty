package com.naoty.gradebook.validation;

@FunctionalInterface
public interface Validator<T> {

    ValidationResult validate(T value);

    default Validator<T> and(Validator<T> other) {
        return value -> this.validate(value).merge(other.validate(value));
    }
}
