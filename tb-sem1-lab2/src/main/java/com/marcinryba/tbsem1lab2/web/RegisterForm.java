package com.marcinryba.tbsem1lab2.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordsMatch
public class RegisterForm {

    @NotBlank(message = "Podaj adres e-mail.")
    @Email(message = "Podaj poprawny adres e-mail.")
    @Size(max = 254, message = "Adres e-mail jest zbyt długi.")
    private String email = "";

    @NotBlank(message = "Podaj hasło.")
    @Pattern(regexp = "(?s)(|.{8,})", message = "Hasło musi mieć co najmniej 8 znaków.")
    @Pattern(regexp = "(?s)(|.*\\p{Lu}.*)", message = "Hasło musi zawierać co najmniej jedną dużą literę.")
    private String password = "";

    @NotBlank(message = "Powtórz hasło.")
    private String confirmPassword = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
