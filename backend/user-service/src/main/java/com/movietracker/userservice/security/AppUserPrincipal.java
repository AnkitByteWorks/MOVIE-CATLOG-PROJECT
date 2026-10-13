package com.movietracker.userservice.security;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.Collection;
import java.util.UUID;

/**
 * Application security principal representing an authenticated user session.
 */
public class AppUserPrincipal extends UserPrincipal {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new AppUserPrincipal.
     *
     * @param id          unique user identifier
     * @param username    unique username handle
     * @param email       user primary email
     * @param password    hashed password
     * @param enabled     account status
     * @param authorities granted security authorities
     */
    public AppUserPrincipal(
            UUID id,
            String username,
            String email,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(id, username, email, password, enabled, authorities);
    }
}
