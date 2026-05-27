package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/training-type.xml")
class TrainingTypeRepositoryTest extends BaseTestRepository<TrainingTypeRepository> {
    @Test
    void findByTrainingTypeName_returnsTrainingType_whenExists() {
        Optional<TrainingType> result = repository.findByTrainingTypeName("Yoga");

        assertThat(result).isPresent();
        assertThat(result.get().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTrainingTypeName_returnsEmpty_whenNotExists() {
        assertThat(repository.findByTrainingTypeName("CrossFit")).isEmpty();
    }
}