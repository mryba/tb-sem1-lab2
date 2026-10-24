package com.marcinryba.tbsem1lab2.identity;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository users;
    private final Clock clock;

    public DatabaseUserDetailsService(UserRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.findByEmail(UserService.normalize(username))
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .accountLocked(user.isLockedAt(clock.instant()))
                .build();
    }
}
