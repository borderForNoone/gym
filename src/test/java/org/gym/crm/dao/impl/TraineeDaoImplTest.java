package org.gym.crm.dao.impl;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DatabaseSetup(value = "/dataset/trainee-dataset.xml")
class TraineeDaoImplTest extends AbstractDaoTest<TraineeDaoImpl> {
    private static final String INVALID_ID_MESSAGE = "ID must be positive and not null, got: %s";

    @Test
    void save_shouldSaveTrainee_whenValid() {
        Trainee trainee = buildTrainee();

        Trainee actual = dao.save(trainee);

        assertThat(actual.getId()).isNotNull();
        assertThat(dao.findById(actual.getId())).isPresent();
        assertThat(actual.getUser().getUsername()).isEqualTo("Simone.Radcliffe");
        assertThat(actual.getUser().getFirstName()).isEqualTo("Simone");
        assertThat(actual.getUser().getLastName()).isEqualTo("Radcliffe");
        assertThat(actual.getUser().getIsActive()).isEqualTo(true);
        assertThat(actual.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 3, 10));
        assertThat(actual.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    void save_shouldThrowException_whenSavingNullTrainee() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.save(null));

        assertThat(exception.getMessage()).isEqualTo("Trainee cannot be null");
    }

    @Test
    void update_shouldUpdateExistingTrainee_whenExists() {
        Trainee trainee = dao.findById(1L).orElseThrow(() -> new AssertionError("Trainee not found"));
        Trainee updated = trainee.toBuilder().address("new address").build();

        Trainee saved = dao.update(updated);
        Trainee actual = dao.findById(saved.getId()).orElseThrow(() -> new AssertionError("Trainee not found"));

        assertThat(actual.getAddress()).isEqualTo("new address");
    }

    @Test
    void update_shouldThrowException_whenIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.update(buildTrainee()));

        assertThat(exception.getMessage()).isEqualTo(String.format(INVALID_ID_MESSAGE, "null"));
    }

    @Test
    void delete_shouldDeleteTrainee_whenExists() {
        dao.delete(1L);

        assertThat(dao.findById(1L)).isEmpty();
        assertThat(dao.findAll().size()).isEqualTo(1);
    }

    @Test
    void delete_shouldThrowException_whenIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.delete((Long) null));

        assertThat(exception.getMessage()).isEqualTo(String.format(INVALID_ID_MESSAGE, "null"));
    }


    @Test
    void findById_shouldReturnTrainee_whenExists() {
        Optional<Trainee> actual = dao.findById(1L);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getUsername()).isEqualTo("Nora.Pemberton");
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenNotFound() {
        Optional<Trainee> actual = dao.findById(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    void findById_shouldThrowException_whenIdIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findById(0L));

        assertThat(exception.getMessage()).isEqualTo(String.format(INVALID_ID_MESSAGE, "0"));
    }

    @Test
    void findAll_shouldReturnAllTrainees_whenExist() {
        List<Trainee> actual = dao.findAll();

        assertThat(actual)
                .hasSize(2)
                .extracting(t -> t.getUser().getUsername())
                .containsExactlyInAnyOrder("Nora.Pemberton", "Ellis.Hargrove");
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .user(buildUser())
                .dateOfBirth(LocalDate.of(2000, 3, 10))
                .address("123 Main St")
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