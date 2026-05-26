package com.gym.crm.repository;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private TraineeRepository traineeRepository;

    private Trainee savedTrainee;

    @BeforeEach
    void setUp() {
        savedTrainee = em.persistFlushFind(buildTrainee("tom.tomas", "Tom", "Tomas"));
    }

    @Test
    void findByUser_Username_returnsTrainee_whenUsernameExists() {
        Optional<Trainee> result = traineeRepository.findByUser_Username("tom.tomas");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getFirstName()).isEqualTo("Tom");
    }

    @Test
    void findByUser_Username_eagerlyLoadsUserAndTrainers() {
        Optional<Trainee> result = traineeRepository.findByUser_Username("tom.tomas");

        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isNotNull();
        assertThat(result.get().getUser().getUsername()).isEqualTo("tom.tomas");
        assertThat(result.get().getTrainers()).isNotNull();
    }

    @Test
    void findByUser_Username_returnsEmpty_whenUsernameNotFound() {
        assertThat(traineeRepository.findByUser_Username("ghost")).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        assertThat(traineeRepository.existsByUser_Username("tom.tomas")).isTrue();
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        assertThat(traineeRepository.existsByUser_Username("nobody")).isFalse();
    }

    @Test
    void save_persistsTrainee_andAssignsId() {
        String username = "user_" + System.currentTimeMillis();

        Trainee trainee = buildTrainee(username, "Fernando", "Torres");

        Trainee persisted = traineeRepository.save(trainee);

        assertThat(persisted.getId()).isNotNull();
    }

    @Test
    void delete_removesTrainee() {
        traineeRepository.delete(savedTrainee);
        em.flush();

        assertThat(traineeRepository.existsByUser_Username("tom.tomas")).isFalse();
    }

    private Trainee buildTrainee(String username, String firstName, String lastName) {
        User user = User.builder().username(username).firstName(firstName).lastName(lastName).password("pass").isActive(true).build();

        return Trainee.builder().user(user).dateOfBirth(LocalDate.of(1995, 5, 20)).address("123 Main St").build();
    }
}