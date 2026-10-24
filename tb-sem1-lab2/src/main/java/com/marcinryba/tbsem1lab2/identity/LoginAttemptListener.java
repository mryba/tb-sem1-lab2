package com.marcinryba.tbsem1lab2.identity;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;

@Component
public class LoginAttemptListener {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final UserRepository users;
    private final Clock clock;

    public LoginAttemptListener(UserRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        users.findByEmail(UserService.normalize(event.getAuthentication().getName()))
                .ifPresent(user -> user.registerFailedLogin(clock.instant(), MAX_FAILED_ATTEMPTS, LOCK_DURATION));
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        users.findByEmail(UserService.normalize(event.getAuthentication().getName()))
                .filter(user -> user.getFailedAttempts() != 0 || user.getLockedUntil() != null)
                .ifPresent(User::resetLockout);
    }
}
