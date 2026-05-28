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
        assertThat(actual.get().getFirstName()).isEqualTo("Julia");
        assertThat(actual.get().getLastName()).isEqualTo("Tomas");
        assertThat(actual.get().getUsername()).isEqualTo(USERNAME);
        assertThat(actual.get().getIsActive()).isTrue();
    }

    @Test
    void findByUsername_shouldReturnEmptyOptional_whenNotFound() {
        Optional<User> actual = repository.findByUsername(NOT_FOUND);

        assertThat(actual).isEmpty();
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        Long id = repository.findByUsername(USERNAME).orElseThrow().getId();

        Optional<User> actual = repository.findById(id);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<User> actual = repository.findById(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        List<User> actual = repository.findAll();

        assertThat(actual).hasSize(5);
        assertThat(actual).extracting(User::getUsername).containsExactlyInAnyOrder("Callum.Whitfield", "Julia.Tomas", "Ellis.Hargrove", "Tom.Trainer",
                        "Simone.Radcliffe");
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        Long id = repository.findByUsername(USERNAME).orElseThrow().getId();

        assertThat(repository.existsById(id)).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        boolean actual = repository.existsById(999L);

        assertThat(actual).isFalse();
    }
}