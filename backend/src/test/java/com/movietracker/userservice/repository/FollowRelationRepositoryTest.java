package com.movietracker.userservice.repository;

import com.movietracker.userservice.config.JpaAuditingConfig;
import com.movietracker.userservice.entity.FollowRelation;
import com.movietracker.userservice.entity.Role;
import com.movietracker.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("FollowRelationRepository DataJpaTest")
class FollowRelationRepositoryTest {

    @Autowired
    private FollowRelationRepository followRelationRepository;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;
    private User charlie;

    @BeforeEach
    void setUp() {
        followRelationRepository.deleteAll();
        userRepository.deleteAll();

        alice = userRepository.save(User.builder()
                .username("alice")
                .email("alice@example.com")
                .password("Pass123!")
                .displayName("Alice")
                .role(Role.USER)
                .enabled(true)
                .build());

        bob = userRepository.save(User.builder()
                .username("bob")
                .email("bob@example.com")
                .password("Pass123!")
                .displayName("Bob")
                .role(Role.USER)
                .enabled(true)
                .build());

        charlie = userRepository.save(User.builder()
                .username("charlie")
                .email("charlie@example.com")
                .password("Pass123!")
                .displayName("Charlie")
                .role(Role.USER)
                .enabled(true)
                .build());
    }

    @Test
    @DisplayName("Should save relation and verify existence and finding by follower and following IDs")
    void shouldSaveAndFindRelation() {
        FollowRelation relation = FollowRelation.builder()
                .follower(alice)
                .following(bob)
                .build();
        followRelationRepository.save(relation);

        assertThat(followRelationRepository.existsByFollowerIdAndFollowingId(alice.getId(), bob.getId())).isTrue();
        assertThat(followRelationRepository.existsByFollowerIdAndFollowingId(bob.getId(), alice.getId())).isFalse();

        Optional<FollowRelation> found = followRelationRepository.findByFollowerIdAndFollowingId(alice.getId(), bob.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFollower().getUsername()).isEqualTo("alice");
        assertThat(found.get().getFollowing().getUsername()).isEqualTo("bob");
    }

    @Test
    @DisplayName("Should find following and followers with pagination")
    void shouldFindFollowingAndFollowers() {
        // Alice follows Bob and Charlie
        followRelationRepository.save(FollowRelation.builder().follower(alice).following(bob).build());
        followRelationRepository.save(FollowRelation.builder().follower(alice).following(charlie).build());

        // Charlie follows Bob
        followRelationRepository.save(FollowRelation.builder().follower(charlie).following(bob).build());

        Page<User> aliceFollowing = followRelationRepository.findFollowing(alice.getId(), PageRequest.of(0, 10));
        assertThat(aliceFollowing.getContent()).hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("bob", "charlie");

        Page<User> bobFollowers = followRelationRepository.findFollowers(bob.getId(), PageRequest.of(0, 10));
        assertThat(bobFollowers.getContent()).hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("alice", "charlie");
    }

    @Test
    @DisplayName("Should accurately count followers and following")
    void shouldCountFollowersAndFollowing() {
        followRelationRepository.save(FollowRelation.builder().follower(alice).following(bob).build());
        followRelationRepository.save(FollowRelation.builder().follower(charlie).following(bob).build());

        assertThat(followRelationRepository.countFollowers(bob.getId())).isEqualTo(2);
        assertThat(followRelationRepository.countFollowing(alice.getId())).isEqualTo(1);
        assertThat(followRelationRepository.countFollowing(bob.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("Should delete relation by follower and following ID")
    void shouldDeleteByFollowerIdAndFollowingId() {
        followRelationRepository.save(FollowRelation.builder().follower(alice).following(bob).build());
        assertThat(followRelationRepository.existsByFollowerIdAndFollowingId(alice.getId(), bob.getId())).isTrue();

        followRelationRepository.deleteByFollowerIdAndFollowingId(alice.getId(), bob.getId());
        assertThat(followRelationRepository.existsByFollowerIdAndFollowingId(alice.getId(), bob.getId())).isFalse();
    }

    @Test
    @DisplayName("Should delete all relations for a user when requested")
    void shouldDeleteAllForUser() {
        // Alice follows Bob; Bob follows Charlie
        followRelationRepository.save(FollowRelation.builder().follower(alice).following(bob).build());
        followRelationRepository.save(FollowRelation.builder().follower(bob).following(charlie).build());

        assertThat(followRelationRepository.count()).isEqualTo(2);

        followRelationRepository.deleteAllForUser(bob.getId());
        assertThat(followRelationRepository.count()).isEqualTo(0);
    }
}
