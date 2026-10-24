package com.marcinryba.tbsem1lab2.config;

import com.marcinryba.tbsem1lab2.identity.DatabaseUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           SecurityContextRepository securityContextRepository,
                                           DatabaseUserDetailsService userDetailsService,
                                           LoginFailureHandler loginFailureHandler,
                                           @Value("${app.remember-me-key}") String rememberMeKey) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/Account/Register", "/Account/Login", "/Account/AccessDenied",
                                "/error", "/webjars/**").permitAll()
                        .anyRequest().authenticated())
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .formLogin(form -> form
                        .loginPage("/Account/Login")
                        .loginProcessingUrl("/Account/Login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler))
                .logout(logout -> logout
                        .logoutUrl("/Account/Logout")
                        .logoutSuccessUrl("/"))
                .rememberMe(remember -> remember
                        .key(rememberMeKey)
                        .rememberMeParameter("rememberMe")
                        .userDetailsService(userDetailsService))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(loginEntryPoint())
                        .accessDeniedPage("/Account/AccessDenied"));
        return http.build();
    }

    private AuthenticationEntryPoint loginEntryPoint() {
        return (request, response, exception) -> {
            String loginPath = request.getContextPath() + "/Account/Login";
            String requested = request.getRequestURI().substring(request.getContextPath().length());
            if (request.getQueryString() != null) {
                requested += "?" + request.getQueryString();
            }
            boolean canReturn = "GET".equals(request.getMethod()) && LocalUrl.isLocal(requested);
            response.sendRedirect(canReturn
                    ? loginPath + "?returnUrl=" + URLEncoder.encode(requested, StandardCharsets.UTF_8)
                    : loginPath);
        };
    }

    private AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            String returnUrl = request.getParameter("returnUrl");
            response.sendRedirect(request.getContextPath() + (LocalUrl.isLocal(returnUrl) ? returnUrl : "/"));
        };
    }
}
