package org.gym.crm.service.impl;

import org.gym.crm.dao.TrainingDao;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.gym.crm.util.TestConstants.DURATION;
import static org.gym.crm.util.TestConstants.FITNESS;
import static org.gym.crm.util.TestConstants.ID;
import static org.gym.crm.util.TestConstants.TRAINING_DATE;
import static org.gym.crm.util.TestConstants.TRAINING_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private TrainingDao trainingDao;
    @InjectMocks
    private TrainingServiceImpl service;

    private Training training;

    @BeforeEach
    void setUp() {
        training = buildTraining();
    }

    @Test
    void create_shouldSaveAndReturnTraining() {
        when(trainingDao.save(training)).thenReturn(training);

        Training actual = service.create(training);

        assertEquals(training, actual);
        verify(trainingDao).save(training);
    }

    private TrainingType buildFitnessType() {
        return TrainingType.builder()
                .id(ID)
                .trainingTypeName(FITNESS)
                .build();
    }

    private Training buildTraining() {
        Trainee trainee = Trainee.builder()
                .id(ID)
                .build();

        Trainer trainer = Trainer.builder()
                .id(ID)
                .build();

        return Training.builder()
                .id(ID)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(buildFitnessType())
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(DURATION)
                .build();
    }
}

