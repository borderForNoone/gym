package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;

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

        User user = actual.get();

        assertThat(user.getFirstName()).isEqualTo("Julia");
        assertThat(user.getLastName()).isEqualTo("Tomas");
        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    void findByUsername_shouldReturnEmptyOptional_whenNotFound() {
        Optional<User> actual = repository.findByUsername(NOT_FOUND);

        assertThat(actual).isEmpty();
    }
}