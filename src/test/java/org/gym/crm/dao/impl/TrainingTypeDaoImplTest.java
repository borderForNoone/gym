package org.gym.crm.dao.impl;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DatabaseSetup(value = "/dataset/training-type.xml")
class TrainingTypeDaoImplTest extends AbstractDaoTest<TrainingTypeDaoImpl> {
    private static final String EMPTY_STRING_EXCEPTION_MESSAGE = "%s cannot be null or empty";

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

        assertThat(exception.getMessage()).isEqualTo(format(EMPTY_STRING_EXCEPTION_MESSAGE, "Training Type Name"));
    }

    @Override
    protected Class<TrainingTypeDaoImpl> getDaoClass() {
        return TrainingTypeDaoImpl.class;
    }
}
