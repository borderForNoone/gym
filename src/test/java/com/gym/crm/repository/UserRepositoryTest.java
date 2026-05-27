package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainee-dataset.xml")
class UserRepositoryTest extends BaseTestRepository<UserRepository> {
    @Test
    void findByUsername_returnsUser_whenExists() {
        Optional<User> result = repository.findByUsername("Julia.Tomas");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Julia");
        assertThat(result.get().getLastName()).isEqualTo("Tomas");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotFound() {
        assertThat(repository.findByUsername("nobody")).isEmpty();
    }
}