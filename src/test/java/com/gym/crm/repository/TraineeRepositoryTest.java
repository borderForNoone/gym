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
        Trainee trainee = buildTrainee("Tom", "Tomas", "Tom.Tomas", LocalDate.of(1995, 1, 15), "123 Test St");

        Trainee saved = repository.save(trainee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isNotNull();
        assertThat(saved.getUser().getUsername()).isEqualTo("Tom.Tomas");
        assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 1, 15));
        assertThat(saved.getAddress()).isEqualTo("123 Test St");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldPersistTraineeWithoutOptionalFields() {
        Trainee trainee = buildTrainee("Jane", "Doe", "Jane.Doe", null, null);

        Trainee saved = repository.save(trainee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDateOfBirth()).isNull();
        assertThat(saved.getAddress()).isNull();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateAddress_whenTraineeProfileUpdated() {
        Trainee existing = getTrainee("Julia.Tomas");
        Trainee updated = existing.toBuilder().address("999 New Address").build();

        repository.save(updated);

        assertThat(getTrainee("Julia.Tomas").getAddress()).isEqualTo("999 New Address");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateIsActive_whenSetActiveCalledOnTrainee() {
        Trainee existing = getTrainee("Julia.Tomas");
        Trainee updated = existing.toBuilder().user(existing.getUser().toBuilder().isActive(false).build()).build();

        repository.save(updated);

        assertThat(getTrainee("Julia.Tomas").getUser().getIsActive()).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdatePassword_whenChangePasswordCalledOnTrainee() {
        Trainee existing = getTrainee("Julia.Tomas");
        Trainee updated = existing.toBuilder().user(existing.getUser().toBuilder().password("new_encoded_pass").build()).build();

        repository.save(updated);

        assertThat(getTrainee("Julia.Tomas").getUser().getPassword()).isEqualTo("new_encoded_pass");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateTrainersList_whenUpdateTrainersListCalled() {
        Trainee existing = getTrainee("Simone.Radcliffe");
        Trainer trainer = getTrainer("Tom.Trainer");
        existing.getTrainers().clear();
        existing.getTrainers().add(trainer);

        repository.save(existing);

        assertThat(countJoinTableRows(existing.getId(), trainer.getId())).isEqualTo(1L);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldRemoveTrainee_whenEntityIsDeleted() {
        Trainee trainee = getTrainee("Julia.Tomas");
        Long traineeId = trainee.getId();

        repository.delete(trainee);

        assertThat(repository.findById(traineeId)).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldRemoveTrainee_whenDeletedByUsername() {
        Trainee trainee = getTrainee("Julia.Tomas");

        repository.delete(trainee);

        assertThat(repository.findByUser_Username("Julia.Tomas")).isEmpty();
    }

    @Test
    @Transactional
    void delete_shouldCascadeDeleteTrainings_whenTraineeDeleted() {
        Trainee trainee = getTrainee("Julia.Tomas");
        Long trainingId = trainee.getTrainings().iterator().next().getId();

        repository.delete(trainee);

        assertThat(trainingRepository.findById(trainingId)).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldRemoveFromJoinTable_whenTraineeDeleted() {
        Trainee trainee = getTrainee("Julia.Tomas");
        Long traineeId = trainee.getId();

        repository.delete(trainee);

        assertThat(countJoinTableRows(traineeId, null)).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void delete_shouldNotAffectTrainer_whenTraineeDeleted() {
        Long trainerId = getTrainer("Tom.Trainer").getId();
        Trainee trainee = getTrainee("Julia.Tomas");

        repository.delete(trainee);

        assertThat(trainerRepository.findById(trainerId)).isPresent();
    }

    @Test
    void findByUser_Username_eagerlyLoadsTrainers_withAllFields() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        Trainer trainer = actual.get().getTrainers().iterator().next();

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainers()).hasSize(1);
        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Trainer");
        assertThat(trainer.getUser().getUsername()).isEqualTo("Tom.Trainer");
        assertThat(trainer.getUser().getIsActive()).isTrue();
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findByUser_Username_eagerlyLoadsTrainings_withAllFields() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        Training training = actual.get().getTrainings().iterator().next();

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainings()).hasSize(1);
        assertThat(training.getTrainingName()).isEqualTo("Morning Cardio");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 5, 10));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("Tom.Trainer");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findByUser_Username_returnsTrainee_whenUsernameExists() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getUsername()).isEqualTo("Julia.Tomas");
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Julia");
        assertThat(actual.get().getUser().getLastName()).isEqualTo("Tomas");
        assertThat(actual.get().getUser().getIsActive()).isTrue();
        assertThat(actual.get().getDateOfBirth()).isEqualTo(LocalDate.of(2000, 3, 10));
        assertThat(actual.get().getAddress()).isEqualTo("10 Sheep St");
    }

    @Test
    void findByUser_Username_eagerlyLoadsUserAndTrainers() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser()).isNotNull();
        assertThat(actual.get().getTrainers()).hasSize(1);
        assertThat(actual.get().getTrainers().iterator().next().getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    @Test
    void findByUser_Username_returnsEmpty_whenUsernameNotFound() {
        Optional<Trainee> actual = repository.findByUser_Username("ghost");

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        assertThat(repository.existsByUser_Username("Ellis.Hargrove")).isTrue();
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        assertThat(repository.existsByUser_Username("nobody")).isFalse();
    }

    private Trainee getTrainee(String username) {
        return entityManager.createQuery("""
                        SELECT t FROM Trainee t
                        JOIN FETCH t.user
                        LEFT JOIN FETCH t.trainers
                        WHERE t.user.username = :username
                        """, Trainee.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    private Trainer getTrainer(String username) {
        return entityManager.createQuery("""
                        SELECT t FROM Trainer t
                        JOIN t.user u
                        WHERE u.username = :username
                        """, Trainer.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    private Trainee buildTrainee(String firstName, String lastName, String username,
                                 LocalDate dateOfBirth, String address) {
        return Trainee.builder()
                .dateOfBirth(dateOfBirth)
                .address(address)
                .user(User.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .username(username)
                        .password("encoded_pass")
                        .isActive(true)
                        .build())
                .build();
    }

    private Long countJoinTableRows(Long traineeId, Long trainerId) {
        String sql = trainerId != null
                ? "SELECT COUNT(*) FROM trainees_trainers WHERE trainee_id = :traineeId AND trainer_id = :trainerId"
                : "SELECT COUNT(*) FROM trainees_trainers WHERE trainee_id = :traineeId";

        var query = entityManager.createNativeQuery(sql)
                .setParameter("traineeId", traineeId);

        if (trainerId != null) {
            query.setParameter("trainerId", trainerId);
        }

        return (Long) query.getSingleResult();
    }
}