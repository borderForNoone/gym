package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainee-dataset.xml")
class UserRepositoryTest extends BaseTestRepository<UserRepository> {
    private static final String USERNAME = "Julia.Tomas";
    private static final String NOT_FOUND = "nobody";

    @Test
    void findByUsername_shouldReturnUser_whenExists() {
        Optional<User> actual = repository.findByUsername(USERNAME);

        assertThat(actual).isPresent();

        User actualUser = actual.get();
        String actualFirstName = actualUser.getFirstName();
        String actualLastName = actualUser.getLastName();
        String actualUsername = actualUser.getUsername();
        Boolean actualIsActive = actualUser.getIsActive();
        String expectedFirstName = "Julia";
        String expectedLastName = "Tomas";
        String expectedUsername = USERNAME;
        Boolean expectedIsActive = true;

        assertThat(actualFirstName).isEqualTo(expectedFirstName);
        assertThat(actualLastName).isEqualTo(expectedLastName);
        assertThat(actualUsername).isEqualTo(expectedUsername);
        assertThat(actualIsActive).isEqualTo(expectedIsActive);
    }

    @Test
    void findByUsername_shouldReturnEmptyOptional_whenNotFound() {
        Optional<User> actual = repository.findByUsername(NOT_FOUND);

        assertThat(actual).isEmpty();
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User julia = repository.findByUsername(USERNAME).orElseThrow();
        Optional<User> actual = repository.findById(julia.getId());

        User actualUser = actual.get();
        String actualUsername = actualUser.getUsername();

        assertThat(actual).isPresent();
        assertThat(actualUsername).isEqualTo(USERNAME);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<User> actual = repository.findById(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        List<User> actual = repository.findAll();

        int actualSize = actual.size();
        int expectedSize = 5;
        List<String> actualUsernames = actual.stream()
                .map(User::getUsername)
                .toList();
        List<String> expectedUsernames = List.of("Callum.Whitfield", "Julia.Tomas", "Ellis.Hargrove", "Tom.Trainer", "Simone.Radcliffe");

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualUsernames).containsExactlyInAnyOrderElementsOf(expectedUsernames);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        User julia = repository.findByUsername(USERNAME).orElseThrow();
        boolean actual = repository.existsById(julia.getId());

        assertThat(actual).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        boolean actual = repository.existsById(999L);

        assertThat(actual).isFalse();
    }
}