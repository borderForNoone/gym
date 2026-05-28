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

        TrainingType actualTrainingType = actual.get();
        Long actualId = actualTrainingType.getId();
        String actualTrainingTypeName = actualTrainingType.getTrainingTypeName();
        Long expectedId = actualId;
        String expectedTrainingTypeName = YOGA;

        assertThat(actualId).isEqualTo(expectedId);
        assertThat(actualTrainingTypeName).isEqualTo(expectedTrainingTypeName);
    }

    @Test
    void findByTrainingTypeName_returnsEmpty_whenNotExists() {
        Optional<TrainingType> actual = repository.findByTrainingTypeName(NOT_FOUND);

        assertThat(actual).isEmpty();
    }

    @Test
    void findAll_returnsAllTrainingTypes() {
        List<TrainingType> actual = repository.findAll();
        int actualSize = actual.size();

        List<String> actualTrainingTypeNames = actual.stream()
                .map(TrainingType::getTrainingTypeName)
                .toList();
        int expectedSize = 3;
        List<String> expectedTrainingTypeNames = List.of("Yoga", "Pilates", "Cardio");

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingTypeNames).containsExactlyInAnyOrderElementsOf(expectedTrainingTypeNames);
    }

    @Test
    void findById_returnsTrainingType_whenExists() {
        TrainingType yoga = repository.findByTrainingTypeName(YOGA).orElseThrow();
        Optional<TrainingType> actual = repository.findById(yoga.getId());

        assertThat(actual).isPresent();

        TrainingType actualTrainingType = actual.get();
        String actualTrainingTypeName = actualTrainingType.getTrainingTypeName();

        assertThat(actualTrainingTypeName).isEqualTo(YOGA);
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<TrainingType> actual = repository.findById(999L);

        assertThat(actual).isEmpty();
    }
}