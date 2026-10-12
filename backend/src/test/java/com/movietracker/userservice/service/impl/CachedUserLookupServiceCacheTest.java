package com.movietracker.userservice.service.impl;

import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.mapper.UserMapper;
import com.movietracker.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(CachedUserLookupServiceCacheTest.TestCacheConfig.class)
@DisplayName("CachedUserLookupService Cache Layer Tests")
class CachedUserLookupServiceCacheTest {

    @Configuration
    @EnableCaching
    @Import(CachedUserLookupService.class)
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("userProfiles");
        }

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        public UserMapper userMapper() {
            return mock(UserMapper.class);
        }
    }

    @Autowired
    private CachedUserLookupService cachedUserLookupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Should cache getPublicProfile calls and evict when evict() is invoked")
    void shouldCacheAndEvictProfile() {
        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("pass")
                .build();
        user.setId(UUID.randomUUID());

        PublicUserResponse response = new PublicUserResponse(
                user.getId(),
                "john_doe",
                "John Doe",
                "Bio",
                null,
                null,
                null,
                0,
                0,
                Instant.now()
        );

        when(userRepository.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(user));
        when(userMapper.toPublicResponse(user)).thenReturn(response);

        // First invocation - should query repository and populate cache
        PublicUserResponse res1 = cachedUserLookupService.getPublicProfile("john_doe");
        assertThat(res1).isNotNull();
        verify(userRepository, times(1)).findByUsernameIgnoreCase("john_doe");

        // Second invocation with different case - should hit cache
        PublicUserResponse res2 = cachedUserLookupService.getPublicProfile("JOHN_DOE");
        assertThat(res2).isNotNull();
        verify(userRepository, times(1)).findByUsernameIgnoreCase("john_doe");

        // Verify cache contains entry with lowercase key
        assertThat(cacheManager.getCache("userProfiles").get("john_doe")).isNotNull();

        // Evict
        cachedUserLookupService.evict("john_doe");
        assertThat(cacheManager.getCache("userProfiles").get("john_doe")).isNull();

        // Third invocation - cache miss, queries repository again
        PublicUserResponse res3 = cachedUserLookupService.getPublicProfile("john_doe");
        assertThat(res3).isNotNull();
        verify(userRepository, times(2)).findByUsernameIgnoreCase("john_doe");
    }
}
