package com.movietracker.userservice.repository;

import com.movietracker.userservice.entity.Role;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("UserRepository DataJpaTest")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = User.builder()
                .username("jane_doe")
                .email("jane@example.com")
                .password("encodedHash123")
                .displayName("Jane Doe")
                .role(Role.USER)
                .enabled(true)
                .emailVerified(true)
                .build();

        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("Should locate user by username")
    void shouldFindByUsername() {
        Optional<User> found = userRepository.findByUsername("jane_doe");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("Should locate user by email")
    void shouldFindByEmail() {
        Optional<User> found = userRepository.findByEmail("jane@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jane_doe");
    }

    @Test
    @DisplayName("Should locate user by matching username or email")
    void shouldFindByUsernameOrEmail() {
        Optional<User> foundByUsername = userRepository.findByUsernameOrEmail("jane_doe", "nomatch@example.com");
        assertThat(foundByUsername).isPresent();

        Optional<User> foundByEmail = userRepository.findByUsernameOrEmail("nomatch", "jane@example.com");
        assertThat(foundByEmail).isPresent();

        Optional<User> notFound = userRepository.findByUsernameOrEmail("nomatch", "nomatch@example.com");
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Should verify user existence by username and email")
    void shouldCheckExists() {
        assertThat(userRepository.existsByUsername("jane_doe")).isTrue();
        assertThat(userRepository.existsByUsername("unknown_user")).isFalse();

        assertThat(userRepository.existsByEmail("jane@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should find user by username ignoring case")
    void shouldFindByUsernameIgnoreCase() {
        Optional<User> found = userRepository.findByUsernameIgnoreCase("JANE_DOE");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jane_doe");
    }

    @Test
    @DisplayName("Should search users by partial username match ignoring case")
    void shouldFindByUsernameContainingIgnoreCase() {
        org.springframework.data.domain.Page<User> result = userRepository.findByUsernameContainingIgnoreCase(
                "ANE",
                org.springframework.data.domain.PageRequest.of(0, 10)
        );
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("jane_doe");
    }
}
