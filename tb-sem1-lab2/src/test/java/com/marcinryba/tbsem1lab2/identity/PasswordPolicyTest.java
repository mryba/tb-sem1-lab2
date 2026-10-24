package com.marcinryba.tbsem1lab2.identity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {

    @Test
    void acceptsStrongPassword() {
        assertThat(PasswordPolicy.violations("Abcdef1!")).isEmpty();
    }

    @Test
    void reportsEveryMissingRule() {
        assertThat(PasswordPolicy.violations("abc"))
                .containsExactly(
                        "Hasło musi mieć co najmniej 8 znaków.",
                        "Hasło musi zawierać co najmniej jedną dużą literę.",
                        "Hasło musi zawierać co najmniej jedną cyfrę.",
                        "Hasło musi zawierać co najmniej jeden znak specjalny.");
    }

    @Test
    void rejectsPasswordLongerThanBcryptLimit() {
        assertThat(PasswordPolicy.violations("Aa1!" + "x".repeat(70))).contains("Hasło jest zbyt długie.");
    }
}
