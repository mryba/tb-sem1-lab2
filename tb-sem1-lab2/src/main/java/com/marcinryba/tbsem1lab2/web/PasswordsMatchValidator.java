package com.marcinryba.tbsem1lab2.web;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, RegisterForm> {

    @Override
    public boolean isValid(RegisterForm form, ConstraintValidatorContext context) {
        if (form.getConfirmPassword() == null || form.getConfirmPassword().isEmpty()
                || form.getPassword().equals(form.getConfirmPassword())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("confirmPassword")
                .addConstraintViolation();
        return false;
    }
}
