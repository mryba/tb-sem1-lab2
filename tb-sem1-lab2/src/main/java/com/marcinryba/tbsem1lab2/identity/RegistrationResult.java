package com.marcinryba.tbsem1lab2.identity;

import java.util.List;

public record RegistrationResult(User user, List<String> errors) {

    public static RegistrationResult success(User user) {
        return new RegistrationResult(user, List.of());
    }

    public static RegistrationResult failure(List<String> errors) {
        return new RegistrationResult(null, errors);
    }

    public boolean succeeded() {
        return errors.isEmpty();
    }
}
