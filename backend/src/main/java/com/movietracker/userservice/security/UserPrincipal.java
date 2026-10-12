package com.movietracker.userservice.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;
import java.util.UUID;

/**
 * Custom Spring Security principal holding user identity metadata including the database UUID.
 */
@Getter
public class UserPrincipal extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String email;

    /**
     * Constructs a new UserPrincipal.
     *
     * @param id          unique user UUID
     * @param username    unique username handle
     * @param email       user primary email
     * @param password    hashed password
     * @param enabled     account status
     * @param authorities granted authority roles
     */
    public UserPrincipal(
            UUID id,
            String username,
            String email,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.email = email;
    }

    /**
     * Accessor method returning the user UUID.
     *
     * @return unique user identifier
     */
    public UUID id() {
        return this.id;
    }
}
