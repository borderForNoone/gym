package org.gym.crm.dao.impl;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DatabaseSetup(value = "/dataset/trainee-dataset.xml")
class TraineeDaoImplTest extends AbstractDaoTest<TraineeDaoImpl> {
    private static final String INVALID_ID_MESSAGE = "ID must be positive and not null, got: %s";

    @Test
    void save_shouldSaveTrainee_whenValid() {
        String uniqueUsername = "Simone.Radcliffe_" + UUID.randomUUID();
        User user = User.builder()
                .firstName("Simone")
                .lastName("Radcliffe")
                .username(uniqueUsername)
                .password("pass444")
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(2000, 3, 10))
                .address("10 Sheep St")
                .build();

        Trainee actual = dao.save(trainee);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUser().getUsername()).isEqualTo(uniqueUsername);
        assertThat(dao.findByUsername(uniqueUsername)).isPresent();
        assertThat(actual.getUser().getFirstName()).isEqualTo("Simone");
        assertThat(actual.getUser().getLastName()).isEqualTo("Radcliffe");
        assertThat(actual.getUser().getIsActive()).isTrue();
        assertThat(actual.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 3, 10));
        assertThat(actual.getAddress()).isEqualTo("10 Sheep St");
    }

    @Test
    void save_shouldThrowException_whenSavingNullTrainee() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.save(null));

        assertThat(exception.getMessage()).isEqualTo("Trainee cannot be null");
    }

    @Test
    void update_shouldUpdateExistingTrainee_whenExists() {
        Trainee trainee = dao.findByUsername("Simone.Radcliffe")
                .orElseThrow(() -> new AssertionError("Trainee not found"));
        Trainee updated = trainee.toBuilder()
                .address("new address")
                .build();

        Trainee saved = dao.update(updated);
        Trainee actual = dao.findByUsername(saved.getUser().getUsername())
                .orElseThrow(() -> new AssertionError("Trainee not found"));

        assertThat(actual.getAddress()).isEqualTo("new address");
    }

    @Test
    void update_shouldThrowException_whenIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.update(buildTrainee()));

        assertThat(exception.getMessage()).isEqualTo(format(INVALID_ID_MESSAGE, "null"));
    }

    @Test
    void delete_shouldRemoveTrainee_whenExists() {
        Trainee trainee = dao.findByUsername("Julia.Tomas")
                .orElseThrow(() -> new AssertionError("Trainee not found"));

        dao.delete(trainee);

        assertThat(dao.findByUsername("Julia.Tomas")).isEmpty();
    }

    @Test
    void delete_shouldThrowException_whenTraineeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.delete(null));

        assertThat(exception.getMessage()).isEqualTo("Trainee cannot be null");
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenUserExists() {
        boolean result = dao.existsByUsername("Julia.Tomas");

        assertThat(result).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenUserNotExists() {
        boolean result = dao.existsByUsername("unknown.user");

        assertThat(result).isFalse();
    }

    @Test
    void findByUsername_shouldReturnTrainee_whenExists() {
        Optional<Trainee> result = dao.findByUsername("Julia.Tomas");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUsername()).isEqualTo("Julia.Tomas");
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUserNotFound() {
        Optional<Trainee> result = dao.findByUsername("non.existing.user");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByUsername_shouldRemoveTrainee_whenExists() {
        dao.deleteByUsername("Nora.Pemberton");

        assertThat(dao.findByUsername("Nora.Pemberton")).isEmpty();
    }

    @Test
    void deleteByUsername_shouldDoNothing_whenUserNotExists() {
        dao.deleteByUsername("ghost.user");

        assertThat(dao.findByUsername("ghost.user")).isEmpty();
    }

    @Test
    void findUnassignedTrainers_shouldReturnTrainersList() {
        List<Trainer> result = dao.findUnassignedTrainers("Nora.Pemberton");

        assertThat(result).isNotNull();
    }

    @Test
    @Transactional
    void updateTrainersList_shouldReplaceTrainersList() {
        List<Trainer> trainers = dao.findAllByUsernames(List.of("Tom.Trainer"));

        dao.updateTrainersList("Julia.Tomas", trainers);

        Trainee updated = dao.findByUsername("Julia.Tomas")
                .orElseThrow(() -> new AssertionError("Trainee not found"));

        assertThat(updated.getTrainers())
                .isNotNull()
                .hasSize(1)
                .extracting(t -> t.getUser().getUsername())
                .containsExactly("Tom.Trainer");
    }

    @Test
    @Transactional
    void updateTrainersList_shouldClearTrainers_whenEmptyListProvided() {
        dao.updateTrainersList("Julia.Tomas", List.of());

        Trainee updated = dao.findByUsername("Julia.Tomas")
                .orElseThrow(() -> new AssertionError("Trainee not found"));

        Hibernate.initialize(updated.getTrainers());

        assertThat(updated.getTrainers())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void findAllByUsernames_shouldReturnEmptyList_whenInputIsEmpty() {
        List<Trainer> result = dao.findAllByUsernames(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUsernames_shouldReturnTrainers_whenTomTrainerRequested() {
        List<Trainer> result = dao.findAllByUsernames(List.of("Tom.Trainer"));

        assertThat(result)
                .hasSize(1)
                .extracting("user.username")
                .containsExactly("Tom.Trainer");
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .user(buildUser())
                .dateOfBirth(LocalDate.of(2000, 3, 10))
                .address("10 Sheep St")
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

    @Override
    protected Class<TraineeDaoImpl> getDaoClass() {
        return TraineeDaoImpl.class;
    }
}