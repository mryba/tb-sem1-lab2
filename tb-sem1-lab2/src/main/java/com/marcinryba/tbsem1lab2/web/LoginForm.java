package com.marcinryba.tbsem1lab2.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank(message = "Podaj adres e-mail.")
    @Email(message = "Podaj poprawny adres e-mail.")
    private String email = "";

    @NotBlank(message = "Podaj hasło.")
    private String password = "";

    private boolean rememberMe;

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

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
