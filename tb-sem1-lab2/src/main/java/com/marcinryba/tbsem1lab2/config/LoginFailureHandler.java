package com.marcinryba.tbsem1lab2.config;

import com.marcinryba.tbsem1lab2.web.LoginForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    static final String GENERIC_ERROR = "Logowanie nie powiodło się. Sprawdź dane lub spróbuj później.";

    private final Validator validator;
    private final SessionFlashMapManager flashMapManager = new SessionFlashMapManager();

    public LoginFailureHandler(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        LoginForm submitted = new LoginForm();
        submitted.setEmail(nullToEmpty(request.getParameter("email")));
        submitted.setPassword(nullToEmpty(request.getParameter("password")));

        List<String> errors = validator.validate(submitted).stream()
                .sorted(Comparator.comparing((ConstraintViolation<LoginForm> v) -> v.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage)
                .toList();
        if (errors.isEmpty()) {
            errors = List.of(GENERIC_ERROR);
        }

        LoginForm echo = new LoginForm();
        echo.setEmail(submitted.getEmail());
        echo.setRememberMe(request.getParameter("rememberMe") != null);

        String loginPath = request.getContextPath() + "/Account/Login";
        FlashMap flashMap = new FlashMap();
        flashMap.put("loginForm", echo);
        flashMap.put("loginErrors", errors);
        flashMap.setTargetRequestPath(loginPath);
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        String returnUrl = request.getParameter("returnUrl");
        String target = LocalUrl.isLocal(returnUrl)
                ? loginPath + "?returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8)
                : loginPath;
        response.sendRedirect(target);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
