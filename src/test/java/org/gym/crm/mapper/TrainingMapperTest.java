package org.gym.crm.mapper;

import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TrainingMapperTest {
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final String TRAINING_TYPE_NAME = "Cardio";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2026, 4, 4);
    private static final int TRAINING_DURATION = 60;
    private static final long VALID_ID = 1L;
    private static final long TRAINEE_ID = 1L;
    private static final long TRAINER_ID = 2L;

    private final TrainingMapper mapper = Mappers.getMapper(TrainingMapper.class);

    @Test
    void toEntity_shouldMapAllFields_whenMapFromTrainingRequestDTO() {
        TrainingRequestDTO trainingRequestDTO = buildTrainingRequestDTO();

        Training training = mapper.toEntity(trainingRequestDTO);

        assertNotNull(training);
        assertEquals(TRAINING_NAME, training.getTrainingName());
        assertEquals(TRAINING_TYPE_NAME, training.getTrainingType().getTrainingTypeName());
        assertEquals(TRAINING_DATE, training.getTrainingDate());
        assertEquals(TRAINING_DURATION, training.getTrainingDuration());
    }

    @Test
    void toDto_shouldMapAllFields_whenMapFromTrainingEntity() {
        Training training = buildTraining();

        TrainingResponseDTO trainingResponseDTO = mapper.toDto(training);

        assertNotNull(trainingResponseDTO);
        assertEquals(TRAINEE_ID, trainingResponseDTO.getTraineeId());
        assertEquals(TRAINER_ID, trainingResponseDTO.getTrainerId());
        assertEquals(TRAINING_NAME, trainingResponseDTO.getTrainingName());
        assertEquals(TRAINING_TYPE_NAME, trainingResponseDTO.getTrainingTypeName());
        assertEquals(TRAINING_DATE, trainingResponseDTO.getTrainingDate());
        assertEquals(TRAINING_DURATION, trainingResponseDTO.getTrainingDuration());
    }

    private TrainingRequestDTO buildTrainingRequestDTO() {
        return TrainingRequestDTO.builder()
                .traineeId(TRAINEE_ID)
                .trainerId(TRAINER_ID)
                .trainingName(TRAINING_NAME)
                .trainingTypeName(TRAINING_TYPE_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }

    private Training buildTraining() {
        User traineeUser = User.builder()
                .id(TRAINEE_ID)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password")
                .isActive(true)
                .build();

        User trainerUser = User.builder()
                .id(TRAINER_ID)
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .password("password")
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .id(TRAINEE_ID)
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("123 Main St")
                .build();

        Trainer trainer = Trainer.builder()
                .id(TRAINER_ID)
                .user(trainerUser)
                .specialization(
                        TrainingType.builder()
                                .trainingTypeName(TRAINING_TYPE_NAME)
                                .build()
                )
                .build();

        return Training.builder()
                .id(VALID_ID)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(TRAINING_NAME)
                .trainingType(
                        TrainingType.builder()
                                .trainingTypeName(TRAINING_TYPE_NAME)
                                .build()
                )
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }
}