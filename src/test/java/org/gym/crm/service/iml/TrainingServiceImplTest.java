package org.gym.crm.service.iml;

import org.gym.crm.dao.TrainingDao;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.gym.crm.util.TestConstants.DURATION;
import static org.gym.crm.util.TestConstants.FITNESS;
import static org.gym.crm.util.TestConstants.ID;
import static org.gym.crm.util.TestConstants.NON_EXISTING_ID;
import static org.gym.crm.util.TestConstants.TRAINING_DATE;
import static org.gym.crm.util.TestConstants.TRAINING_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private TrainingDao trainingDao;
    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = buildTraining();
    }

    @Test
    void create_shouldSaveAndReturnTraining() {
        when(trainingDao.save(training)).thenReturn(training);

        Training actual = trainingService.create(training);

        assertEquals(training, actual);
        verify(trainingDao).save(training);
    }

    @Test
    void findById_shouldReturnTraining_whenExists() {
        when(trainingDao.findById(ID)).thenReturn(Optional.of(training));

        Optional<Training> actual = trainingService.findById(ID);

        assertTrue(actual.isPresent());
        assertEquals(training, actual.get());
        verify(trainingDao).findById(ID);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(trainingDao.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        Optional<Training> actual = trainingService.findById(NON_EXISTING_ID);

        assertTrue(actual.isEmpty());
        verify(trainingDao).findById(NON_EXISTING_ID);
    }

    @Test
    void findAll_shouldReturnAllTrainings() {
        List<Training> expected = List.of(training);
        when(trainingDao.findAll()).thenReturn(expected);

        List<Training> actual = trainingService.findAll();

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.getFirst(), actual.getFirst());
        verify(trainingDao).findAll();
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

