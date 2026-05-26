package com.gym.crm.repository;

import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = em.persistFlushFind(buildUser("john.doe", "John", "Doe"));
    }

    @Test
    void findByUsername_returnsUser_whenExists() {
        Optional<User> result = userRepository.findByUsername("john.doe");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotFound() {
        assertThat(userRepository.findByUsername("nobody")).isEmpty();
    }

    @Test
    void findByUsername_returnsEmpty_afterUserDeleted() {
        userRepository.delete(savedUser);
        em.flush();

        assertThat(userRepository.findByUsername("john.doe")).isEmpty();
    }

    @Test
    void save_persistsNewUser_andAssignsId() {
        User newUser = buildUser("jane.doe", "Jane", "Doe");

        User persisted = userRepository.save(newUser);

        assertThat(persisted.getId()).isNotNull();
        assertThat(userRepository.findById(persisted.getId())).isPresent();
    }

    @Test
    void save_updatesExistingUser() {
        User updated = savedUser.toBuilder().firstName("Jonathan").build();
        userRepository.save(updated);
        em.flush();
        em.clear();

        User reloaded = userRepository.findByUsername("john.doe").orElseThrow();
        assertThat(reloaded.getFirstName()).isEqualTo("Jonathan");
    }

    @Test
    void findAll_returnsAllPersistedUsers() {
        em.persistAndFlush(buildUser("second.user", "Second", "User"));

        assertThat(userRepository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void deleteById_removesUser() {
        userRepository.deleteById(savedUser.getId());
        em.flush();

        assertThat(userRepository.findByUsername("john.doe")).isEmpty();
    }

    private User buildUser(String username, String firstName, String lastName) {
        return User.builder().username(username).firstName(firstName).lastName(lastName).password("secret").isActive(true).build();
    }
}