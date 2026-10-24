package com.marcinryba.tbsem1lab2.identity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static List<String> violations(String password) {
        List<String> errors = new ArrayList<>();
        if (password.length() < MIN_LENGTH) {
            errors.add("Hasło musi mieć co najmniej 8 znaków.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            errors.add("Hasło jest zbyt długie.");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            errors.add("Hasło musi zawierać co najmniej jedną małą literę.");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            errors.add("Hasło musi zawierać co najmniej jedną dużą literę.");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            errors.add("Hasło musi zawierać co najmniej jedną cyfrę.");
        }
        if (password.chars().allMatch(Character::isLetterOrDigit)) {
            errors.add("Hasło musi zawierać co najmniej jeden znak specjalny.");
        }
        return errors;
    }
}
