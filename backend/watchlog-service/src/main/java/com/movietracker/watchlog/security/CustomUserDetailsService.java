package com.movietracker.watchlog.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Stateless UserDetailsService adapter reconstructing user principals from JWT context.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username cannot be empty");
        }
        return new AppUserPrincipal(
                UUID.randomUUID(),
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public UserDetails createUserPrincipal(UUID userId, String username, String email, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = (roles != null && !roles.isEmpty())
                ? roles.stream().map(r -> new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r)).toList()
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new AppUserPrincipal(
                userId,
                username,
                email,
                authorities
        );
    }
}
