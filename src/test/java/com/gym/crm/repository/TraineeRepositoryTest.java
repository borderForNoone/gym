package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainee-dataset.xml")
@DatabaseTearDown(value = "/dataset/trainee-dataset.xml", type = DatabaseOperation.DELETE_ALL)
class TraineeRepositoryTest extends BaseTestRepository<TraineeRepository> {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TrainingRepository trainingRepository;
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetSequences() {
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1000");
        jdbcTemplate.execute("ALTER TABLE trainees ALTER COLUMN id RESTART WITH 1000");
        jdbcTemplate.execute("ALTER TABLE trainers ALTER COLUMN id RESTART WITH 1000");
        jdbcTemplate.execute("ALTER TABLE trainings ALTER COLUMN id RESTART WITH 1000");
        jdbcTemplate.execute("ALTER TABLE training_types ALTER COLUMN id RESTART WITH 1000");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldPersistTraineeWithUser() {
        User user = User.builder()
                .firstName("Tom")
                .lastName("Tomas")
                .username("Tom.Tomas")
                .password("encoded_pass")
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder()
                .dateOfBirth(LocalDate.of(1995, 1, 15))
                .address("123 Test St")
                .user(user)
                .build();

        Trainee saved = repository.save(trainee);

        Long traineeId = saved.getId();
        Long userId = saved.getUser().getId();
        String username = saved.getUser().getUsername();
        LocalDate birthDate = saved.getDateOfBirth();
        String address = saved.getAddress();

        assertThat(traineeId).isNotNull();
        assertThat(userId).isNotNull();
        assertThat(username).isEqualTo("Tom.Tomas");
        assertThat(birthDate).isEqualTo(LocalDate.of(1995, 1, 15));
        assertThat(address).isEqualTo("123 Test St");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldPersistTraineeWithoutOptionalFields() {
        User user = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .username("Jane.Doe")
                .password("encoded_pass")
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder().user(user).build();

        Trainee saved = repository.save(trainee);

        Long traineeId = saved.getId();
        LocalDate birthDate = saved.getDateOfBirth();
        String address = saved.getAddress();

        assertThat(traineeId).isNotNull();
        assertThat(birthDate).isNull();
        assertThat(address).isNull();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateAddress_whenTraineeProfileUpdated() {
        Trainee existing = repository.findByUser_Username("Julia.Tomas").orElseThrow();
        Trainee updated = existing.toBuilder().address("999 New Address").build();

        repository.save(updated);

        Trainee found = repository.findByUser_Username("Julia.Tomas").orElseThrow();

        assertThat(found.getAddress()).isEqualTo("999 New Address");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateIsActive_whenSetActiveCalledOnTrainee() {
        Trainee existing = repository.findByUser_Username("Julia.Tomas").orElseThrow();
        User updatedUser = existing.getUser().toBuilder().isActive(false).build();
        Trainee updated = existing.toBuilder().user(updatedUser).build();

        repository.save(updated);

        Trainee found = repository.findByUser_Username("Julia.Tomas").orElseThrow();
        assertThat(found.getUser().getIsActive()).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdatePassword_whenChangePasswordCalledOnTrainee() {
        Trainee existing = repository.findByUser_Username("Julia.Tomas").orElseThrow();
        User updatedUser = existing.getUser().toBuilder().password("new_encoded_pass").build();
        Trainee updated = existing.toBuilder().user(updatedUser).build();

        repository.save(updated);

        Trainee found = repository.findByUser_Username("Julia.Tomas").orElseThrow();
        assertThat(found.getUser().getPassword()).isEqualTo("new_encoded_pass");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateTrainersList_whenUpdateTrainersListCalled() {
        Trainee existing = repository.findByUser_Username("Simone.Radcliffe").orElseThrow();
        Trainer trainer = trainerRepository.findByUser_Username("Tom.Trainer").orElseThrow();
        existing.getTrainers().clear();
        existing.getTrainers().add(trainer);
        Long traineeId = existing.getId();
        Long trainerId = trainer.getId();

        repository.save(existing);

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM trainees_trainers WHERE trainee_id = :traineeId AND trainer_id = :trainerId")
                .setParameter("traineeId", traineeId)
                .setParameter("trainerId", trainerId)
                .getSingleResult();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldRemoveTrainee_whenEntityIsDeleted() {
        String username = "Julia.Tomas";
        Trainee trainee = repository.findByUser_Username(username).orElseThrow();
        Long traineeId = trainee.getId();

        repository.delete(trainee);

        assertThat(repository.findById(traineeId)).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldCascadeDeleteUser_whenTraineeIsDeleted() {
        Trainee trainee = repository.findByUser_Username("Julia.Tomas").orElseThrow();
        Long traineeId = trainee.getId();
        Long userId = trainee.getUser().getId();

        repository.delete(trainee);

        assertThat(repository.findById(traineeId)).isEmpty();
        assertThat(entityManager.find(User.class, userId)).isNull();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldRemoveTrainee_whenDeletedByUsername() {
        String username = "Julia.Tomas";
        Trainee trainee = repository.findByUser_Username(username).orElseThrow();

        repository.delete(trainee);

        assertThat(repository.findByUser_Username(username)).isEmpty();
    }

    @Test
    @Transactional
    void delete_shouldCascadeDeleteTrainings_whenTraineeDeleted() {
        String username = "Julia.Tomas";
        Trainee trainee = repository.findByUser_Username(username).orElseThrow();
        Long trainingId = trainee.getTrainings().iterator().next().getId();

        repository.delete(trainee);

        assertThat(trainingRepository.findById(trainingId)).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldRemoveFromJoinTable_whenTraineeDeleted() {
        String username = "Julia.Tomas";
        Trainee trainee = repository.findByUser_Username(username).orElseThrow();
        Long traineeId = trainee.getId();

        repository.delete(trainee);

        Long count = (Long) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM trainees_trainers
                WHERE trainee_id = :traineeId
                """).setParameter("traineeId", traineeId).getSingleResult();

        assertThat(count).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldNotAffectTrainer_whenTraineeDeleted() {
        String traineeUsername = "Julia.Tomas";
        String trainerUsername = "Tom.Trainer";
        Long trainerId = trainerRepository.findByUser_Username(trainerUsername).orElseThrow().getId();
        Trainee trainee = repository.findByUser_Username(traineeUsername).orElseThrow();

        repository.delete(trainee);

        assertThat(trainerRepository.findById(trainerId)).isPresent();
    }

    @Test
    void findByUser_Username_eagerlyLoadsTrainers_withAllFields() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        Trainee trainee = actual.get();

        assertThat(actual).isPresent();
        assertThat(trainee.getTrainers()).hasSize(1)
                .satisfiesExactly(trainer -> {
                    assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom");
                    assertThat(trainer.getUser().getLastName()).isEqualTo("Trainer");
                    assertThat(trainer.getUser().getUsername()).isEqualTo("Tom.Trainer");
                    assertThat(trainer.getUser().getIsActive()).isTrue();
                    assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Cardio");
                });
    }

    @Test
    void findByUser_Username_eagerlyLoadsTrainings_withAllFields() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        Trainee trainee = actual.get();
        Set<Training> trainings = trainee.getTrainings();
        Training training = trainings.iterator().next();

        assertThat(actual).isPresent();
        assertThat(trainings).hasSize(1);
        assertThat(training.getId()).isEqualTo(1L);
        assertThat(training.getTrainingName()).isEqualTo("Morning Cardio");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 5, 10));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("Tom.Trainer");
        assertThat(training.getTrainingType().getId()).isEqualTo(1L);
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findByUser_Username_returnsTrainee_whenUsernameExists() {
        Optional<Trainee> actualOptional = repository.findByUser_Username("Julia.Tomas");

        assertThat(actualOptional).isPresent();

        Trainee actualTrainee = actualOptional.get();
        String actualUsername = actualTrainee.getUser().getUsername();
        String expectedUsername = "Julia.Tomas";
        String actualFirstName = actualTrainee.getUser().getFirstName();
        String expectedFirstName = "Julia";

        assertThat(actualUsername).isEqualTo(expectedUsername);
        assertThat(actualFirstName).isEqualTo(expectedFirstName);
    }

    @Test
    void findByUser_Username_eagerlyLoadsUserAndTrainers() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        assertThat(actual).isPresent();

        Trainee trainee = actual.get();
        User user = trainee.getUser();
        Set<Trainer> trainers = trainee.getTrainers();
        int trainerCount = trainers.size();
        String trainerUsername = trainers.iterator().next().getUser().getUsername();

        assertThat(user).isNotNull();
        assertThat(trainerCount).isEqualTo(1);
        assertThat(trainerUsername).isEqualTo("Tom.Trainer");
    }

    @Test
    void findByUser_Username_returnsEmpty_whenUsernameNotFound() {
        Optional<Trainee> actual = repository.findByUser_Username("ghost");

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        boolean actualExists = repository.existsByUser_Username("Ellis.Hargrove");
        boolean expectedExists = true;

        assertThat(actualExists).isEqualTo(expectedExists);
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        boolean exists = repository.existsByUser_Username("nobody");

        assertThat(exists).isFalse();
    }
}