package com.movietracker.userservice.entity;

/**
 * User authorization roles supported across the Movie Tracker platform.
 */
public enum Role {

    /**
     * Standard registered user with default application permissions.
     */
    USER,

    /**
     * System administrator with full operational privileges.
     */
    ADMIN,

    /**
     * Platform moderator with content curation and moderation capabilities.
     */
    MODERATOR
}
