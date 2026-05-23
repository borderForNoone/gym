package org.gym.crm.dao.impl;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DatabaseSetup(value = "/dataset/trainer.xml")
class TrainerDaoImplTest extends AbstractDaoTest<TrainerDaoImpl> {
    private static final String INVALID_ID_MESSAGE = "ID must be positive and not null, got: %s";

    @Test
    void save_shouldSaveTrainer_whenValid() {
        Trainer trainer = buildTrainer();
        Trainer actual = dao.save(trainer);

        assertThat(actual.getUser().getUsername()).isEqualTo("Simone.Radcliffe");
        assertThat(dao.findByUsername(actual.getUser().getUsername())).isPresent();
        assertThat(actual.getUser().getFirstName()).isEqualTo("Simone");
        assertThat(actual.getUser().getLastName()).isEqualTo("Radcliffe");
        assertThat(actual.getUser().getIsActive()).isTrue();
        assertThat(actual.getSpecialization().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void save_shouldThrowException_whenSavingNullTrainer() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.save(null));

        assertThat(exception.getMessage()).isEqualTo("Trainer cannot be null");
    }

    @Test
    void update_shouldUpdateExistingTrainer_whenExists() {
        Trainer trainer = dao.findByUsername("Callum.Whitfield")
                .orElseThrow(() -> new AssertionError("Trainer not found"));

        TrainingType newTrainingType = TrainingType.builder()
                .id(11L)
                .trainingTypeName("Pilates")
                .build();
        Trainer updated = trainer.toBuilder()
                .specialization(newTrainingType)
                .build();

        Trainer saved = dao.update(updated);
        Trainer actual = dao.findByUsername(saved.getUser().getUsername())
                .orElseThrow(() -> new AssertionError("Trainer not found"));

        assertThat(actual.getSpecialization().getTrainingTypeName())
                .isEqualTo("Pilates");
    }

    @Test
    void update_shouldThrowException_whenIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.update(buildTrainer()));

        assertThat(exception.getMessage()).isEqualTo(format(INVALID_ID_MESSAGE, "null"));
    }

    @Test
    void findNotAssignedToTrainee_shouldReturnOnlyUnassignedTrainers() {
        List<Trainer> actual = dao.findNotAssignedToTrainee("some.trainee");

        assertThat(actual)
                .extracting(t -> t.getUser().getUsername())
                .containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findNotAssignedToTrainee_shouldReturnAll_whenTraineeHasNoTrainings() {
        List<Trainer> actual = dao.findNotAssignedToTrainee("Unknown.User");

        assertThat(actual).isNotEmpty();
    }

    @Test
    void findNotAssignedToTrainee_shouldThrowException_whenUsernameBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> dao.findNotAssignedToTrainee(""));

        assertThat(exception.getMessage()).isEqualTo("Trainee Username cannot be null or empty");
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenTrainerExists() {
        boolean result = dao.existsByUsername("Callum.Whitfield");

        assertThat(result).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenTrainerNotExists() {
        boolean result = dao.existsByUsername("No.Such.User");

        assertThat(result).isFalse();
    }

    @Test
    void existsByUsername_shouldThrowException_whenBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.existsByUsername(" "));

        assertThat(exception.getMessage())
                .isEqualTo("Username cannot be null or empty");
    }

    @Test
    void findByUsername_shouldReturnTrainer_whenExists() {
        Optional<Trainer> actual = dao.findByUsername("Callum.Whitfield");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Callum");
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        Optional<Trainer> actual = dao.findByUsername("unknown.user");

        assertThat(actual).isEmpty();
    }

    @Test
    void findByUsername_shouldThrowException_whenBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findByUsername(null));

        assertThat(exception.getMessage())
                .isEqualTo("Username cannot be null or empty");
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .user(buildUser())
                .specialization(buildTrainingType())
                .build();
    }

    private User buildUser() {
        return User.builder()
                .firstName("Simone")
                .lastName("Radcliffe")
                .username("Simone.Radcliffe")
                .password("pass444")
                .isActive(true)
                .build();
    }

    private TrainingType buildTrainingType() {
        return TrainingType.builder()
                .id(10L)
                .trainingTypeName("Yoga")
                .build();
    }
}
