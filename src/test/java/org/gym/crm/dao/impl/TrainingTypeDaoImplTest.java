package org.gym.crm.dao.impl;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DatabaseSetup(value = "/dataset/training-type.xml")
class TrainingTypeDaoImplTest extends AbstractDaoTest<TrainingTypeDaoImpl> {
    private static final String INVALID_ID_MESSAGE = "ID must be positive and not null, got: %s";
    private static final String EMPTY_STRING_EXCEPTION_MESSAGE = "%s cannot be null or empty";

    @Test
    void findById_shouldReturnTrainingType_whenExists() {
        Optional<TrainingType> actual = dao.findById(1L);

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenNotFound() {
        Optional<TrainingType> actual = dao.findById(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    void findById_shouldThrowException_whenIdIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findById(0L));

        assertThat(exception.getMessage()).isEqualTo(String.format(INVALID_ID_MESSAGE, "0"));
    }

    @Test
    void findByTrainingTypeName_shouldReturnTrainingType_whenExists() {
        Optional<TrainingType> actual = dao.findByTrainingTypeName("Yoga");

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTrainingTypeName_shouldReturnEmptyOptional_whenNotFound() {
        Optional<TrainingType> actual = dao.findByTrainingTypeName("Non-Existing");

        assertThat(actual).isEmpty();
    }

    @Test
    void findByTrainingTypeName_shouldThrowException_whenNullOrEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findByTrainingTypeName(" "));

        assertThat(exception.getMessage()).isEqualTo(String.format(EMPTY_STRING_EXCEPTION_MESSAGE, "Training Type Name"));
    }

    @Test
    void findAll_shouldReturnAllTrainingTypes_whenExist() {
        List<TrainingType> actual = dao.findAll();

        assertThat(actual)
                .hasSize(3)
                .extracting(TrainingType::getTrainingTypeName)
                .containsExactlyInAnyOrder("Yoga", "Pilates", "Cardio");
    }

    @Override
    protected Class<TrainingTypeDaoImpl> getDaoClass() {
        return TrainingTypeDaoImpl.class;
    }
}
