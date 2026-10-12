package com.movietracker.userservice.service.impl;

import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.exception.ResourceNotFoundException;
import com.movietracker.userservice.mapper.UserMapper;
import com.movietracker.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service providing cached public profile retrieval and targeted cache eviction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CachedUserLookupService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Looks up a user's public profile, caching results in Redis by lowercase username.
     *
     * @param username username handle
     * @return public user profile view
     */
    @Cacheable(value = "userProfiles", key = "#username.toLowerCase()", unless = "#result == null")
    @Transactional(readOnly = true)
    public PublicUserResponse getPublicProfile(String username) {
        log.debug("Cache miss for user profile: '{}', querying database", username);
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toPublicResponse(user);
    }

    /**
     * Alias for {@link #getPublicProfile(String)}.
     *
     * @param username username handle
     * @return public user profile view
     */
    public PublicUserResponse getByUsername(String username) {
        return getPublicProfile(username);
    }

    /**
     * Evicts the cached profile for the given username handle from Redis.
     *
     * @param username username handle whose cache entry should be purged
     */
    @CacheEvict(value = "userProfiles", key = "#username.toLowerCase()")
    public void evict(String username) {
        log.debug("Evicted user profile cache for username: {}", username);
    }
}
