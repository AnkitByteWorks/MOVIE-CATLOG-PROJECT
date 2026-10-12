package com.movietracker.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity representing a registered user account in the Movie Tracker platform.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username")
        }
)
public class User extends BaseEntity {

    /**
     * Unique handle for user authentication and identification.
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Unique primary email address associated with the user account.
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Securely hashed representation of the user password.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Friendly display name shown in community and profile interfaces.
     */
    @Column(name = "display_name")
    private String displayName;

    /**
     * Role determining assigned privileges and authority levels.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /**
     * Flag indicating whether the account is currently active and permitted to authenticate.
     */
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Flag indicating whether the user email address has been verified.
     */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /**
     * Timestamp of the most recent successful login activity.
     */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * Personal biography or user self-introduction.
     */
    @Column(name = "bio", length = 200)
    private String bio;

    /**
     * URL link pointing to user avatar picture.
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * User geographic location.
     */
    @Column(name = "location", length = 100)
    private String location;

    /**
     * Personal website or portfolio URL.
     */
    @Column(name = "website", length = 200)
    private String website;

    /**
     * Count of accounts following this user.
     */
    @Builder.Default
    @Column(name = "follower_count", nullable = false)
    private Integer followerCount = 0;

    /**
     * Count of accounts this user is currently following.
     */
    @Builder.Default
    @Column(name = "following_count", nullable = false)
    private Integer followingCount = 0;

    @PrePersist
    @PreUpdate
    public void ensureCountsNotNull() {
        if (followerCount == null) {
            followerCount = 0;
        }
        if (followingCount == null) {
            followingCount = 0;
        }
    }
}
