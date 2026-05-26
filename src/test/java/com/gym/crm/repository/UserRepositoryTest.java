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
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = entityManager.persistFlushFind(buildUser("tom.tomas", "Tom", "Tomas"));
    }

    @Test
    void findByUsername_returnsUser_whenExists() {
        Optional<User> result = userRepository.findByUsername("tom.tomas");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Tom");
        assertThat(result.get().getLastName()).isEqualTo("Tomas");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotFound() {
        assertThat(userRepository.findByUsername("nobody")).isEmpty();
    }

    @Test
    void findByUsername_returnsEmpty_afterUserDeleted() {
        userRepository.delete(savedUser);
        entityManager.flush();

        assertThat(userRepository.findByUsername("tom.tomas")).isEmpty();
    }

    @Test
    void save_persistsNewUser_andAssignsId() {
        User newUser = buildUser("julia.tomas", "Julia", "Tomas");

        User persisted = userRepository.save(newUser);

        assertThat(persisted.getId()).isNotNull();
        assertThat(userRepository.findById(persisted.getId())).isPresent();
    }

    @Test
    void save_updatesExistingUser() {
        User user = userRepository.findById(savedUser.getId()).orElseThrow();
        User updatedUser = user.toBuilder().firstName("Tomas").build();

        userRepository.save(updatedUser);

        entityManager.flush();
        entityManager.clear();

        User reloaded = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(reloaded.getFirstName()).isEqualTo("Tomas");
    }

    @Test
    void findAll_returnsAllPersistedUsers() {
        entityManager.persistAndFlush(buildUser("second.user", "Second", "User"));

        assertThat(userRepository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void deleteById_removesUser() {
        userRepository.deleteById(savedUser.getId());
        entityManager.flush();

        assertThat(userRepository.findByUsername("tom.tomas")).isEmpty();
    }

    private User buildUser(String username, String firstName, String lastName) {
        return User.builder().username(username).firstName(firstName).lastName(lastName).password("secret").isActive(true).build();
    }
}