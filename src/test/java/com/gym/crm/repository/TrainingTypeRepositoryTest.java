package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/training-type.xml")
class TrainingTypeRepositoryTest extends BaseTestRepository<TrainingTypeRepository> {
    private static final String YOGA = "Yoga";
    private static final String NOT_FOUND = "CrossFit";

    @Test
    void findByTrainingTypeName_returnsTrainingType_whenExists() {
        Optional<TrainingType> result = repository.findByTrainingTypeName(YOGA);

        assertThat(result).isPresent();

        TrainingType trainingType = result.get();

        assertThat(trainingType.getId()).isNotNull();
        assertThat(trainingType.getTrainingTypeName()).isEqualTo(YOGA);
    }

    @Test
    void findByTrainingTypeName_returnsEmpty_whenNotExists() {
        Optional<TrainingType> result = repository.findByTrainingTypeName(NOT_FOUND);

        assertThat(result).isEmpty();
    }
}