package com.gym.crm.repository;

import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


class TrainingTypeRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @BeforeEach
    void setUp() {
        em.persistAndFlush(TrainingType.builder().trainingTypeName("Yoga").build());
        em.persistAndFlush(TrainingType.builder().trainingTypeName("Pilates").build());
    }

    @Test
    void findByTrainingTypeName_returnsType_whenNameExists() {
        Optional<TrainingType> result = trainingTypeRepository.findByTrainingTypeName("Yoga");

        assertThat(result).isPresent();
        assertThat(result.get().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTrainingTypeName_returnsEmpty_whenNameNotFound() {
        assertThat(trainingTypeRepository.findByTrainingTypeName("CrossFit")).isEmpty();
    }

    @Test
    void findByTrainingTypeName_isCaseSensitive() {
        assertThat(trainingTypeRepository.findByTrainingTypeName("yoga")).isEmpty();
    }

    @Test
    void save_persistsNewType_andAssignsId() {
        TrainingType boxing = TrainingType.builder().trainingTypeName("Boxing").build();

        TrainingType saved = trainingTypeRepository.save(boxing);

        assertThat(saved.getId()).isNotNull();
        assertThat(trainingTypeRepository.findByTrainingTypeName("Boxing")).isPresent();
    }

    @Test
    void findAll_returnsAllPersistedTypes() {
        assertThat(trainingTypeRepository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void deleteById_removesType() {
        TrainingType pilates = trainingTypeRepository.findByTrainingTypeName("Pilates").orElseThrow();

        trainingTypeRepository.deleteById(pilates.getId());
        em.flush();

        assertThat(trainingTypeRepository.findByTrainingTypeName("Pilates")).isEmpty();
    }
}