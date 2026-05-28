package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/training-type.xml")
class TrainingTypeRepositoryTest extends BaseTestRepository<TrainingTypeRepository> {
    private static final String YOGA = "Yoga";
    private static final String NOT_FOUND = "CrossFit";

    @Test
    void findByTrainingTypeName_returnsTrainingType_whenExists() {
        Optional<TrainingType> actual = repository.findByTrainingTypeName(YOGA);

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isNotNull();
        assertThat(actual.get().getTrainingTypeName()).isEqualTo(YOGA);
    }

    @Test
    void findByTrainingTypeName_returnsEmpty_whenNotExists() {
        Optional<TrainingType> actual = repository.findByTrainingTypeName(NOT_FOUND);

        assertThat(actual).isEmpty();
    }

    @Test
    void findAll_returnsAllTrainingTypes() {
        List<TrainingType> actual = repository.findAll();

        assertThat(actual).hasSize(3);
        assertThat(actual).extracting(TrainingType::getTrainingTypeName).containsExactlyInAnyOrder("Yoga", "Pilates", "Cardio");
    }
}