package com.movietracker.userservice.repository;

import com.movietracker.userservice.entity.FollowRelation;
import com.movietracker.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for managing directional follow associations between users.
 */
@Repository
public interface FollowRelationRepository extends JpaRepository<FollowRelation, UUID> {

    /**
     * Checks if a follow relationship exists from follower to target following.
     *
     * @param followerId  origin user UUID
     * @param followingId target user UUID
     * @return true if relationship exists
     */
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * Retrieves an existing follow relationship between two users.
     *
     * @param followerId  origin user UUID
     * @param followingId target user UUID
     * @return Optional containing the FollowRelation if active
     */
    Optional<FollowRelation> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * Retrieves a paginated list of accounts followed by the specified user.
     *
     * @param userId   UUID of the follower
     * @param pageable pagination parameters
     * @return Page of followed User entities
     */
    @Query("SELECT f.following FROM FollowRelation f WHERE f.follower.id = :userId")
    Page<User> findFollowing(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Retrieves a paginated list of accounts following the specified user.
     *
     * @param userId   UUID of the followed user
     * @param pageable pagination parameters
     * @return Page of follower User entities
     */
    @Query("SELECT f.follower FROM FollowRelation f WHERE f.following.id = :userId")
    Page<User> findFollowers(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Counts how many users the specified user is following.
     *
     * @param userId user UUID
     * @return following count
     */
    @Query("SELECT COUNT(f) FROM FollowRelation f WHERE f.follower.id = :userId")
    long countFollowing(@Param("userId") UUID userId);

    /**
     * Counts how many followers the specified user has.
     *
     * @param userId user UUID
     * @return follower count
     */
    @Query("SELECT COUNT(f) FROM FollowRelation f WHERE f.following.id = :userId")
    long countFollowers(@Param("userId") UUID userId);

    /**
     * Deletes a directional follow relationship between two accounts.
     *
     * @param followerId  follower user UUID
     * @param followingId target user UUID
     */
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /**
     * Deletes all follow records where the user is either the follower or the followed target.
     *
     * @param userId user UUID
     */
    @Modifying
    @Query("DELETE FROM FollowRelation f WHERE f.follower.id = :userId OR f.following.id = :userId")
    void deleteAllForUser(@Param("userId") UUID userId);
}
