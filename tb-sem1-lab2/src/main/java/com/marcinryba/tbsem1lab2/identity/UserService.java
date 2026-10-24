package com.marcinryba.tbsem1lab2.identity;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private static final String DUPLICATE_EMAIL = "Konto z tym adresem e-mail już istnieje.";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public RegistrationResult register(String email, String rawPassword) {
        String normalized = normalize(email);

        List<String> errors = new ArrayList<>(PasswordPolicy.violations(rawPassword));
        if (users.existsByEmail(normalized)) {
            errors.add(DUPLICATE_EMAIL);
        }
        if (!errors.isEmpty()) {
            return RegistrationResult.failure(errors);
        }

        try {
            User user = users.saveAndFlush(new User(normalized, passwordEncoder.encode(rawPassword)));
            return RegistrationResult.success(user);
        } catch (DataIntegrityViolationException e) {
            return RegistrationResult.failure(List.of(DUPLICATE_EMAIL));
        }
    }
}
