package com.gym.crm.util;

import com.gym.crm.facade.dto.AuthResponseDTO;
import com.gym.crm.facade.dto.TraineeResponseDTO;
import com.gym.crm.facade.dto.TrainerResponseDTO;
import com.gym.crm.facade.dto.TrainingResponseDTO;

import java.time.LocalDate;

public class TestConstants {
    private TestConstants() {
    }

    public static final String FIRST_NAME = "Kaden";
    public static final String LAST_NAME = "Voss";
    public static final String USERNAME = "Kaden.Voss";
    public static final String ADDRESS = "10 Sheep St";
    public static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    public static final String TRAINER_USERNAME = "Mira.Calder";
    public static final String FITNESS = "FITNESS";
    public static final Long ID = 1L;
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final String TRAINING_TYPE_NAME = "Cardio";
    private static final String TRAINER_FIRST_NAME = "Owen";
    private static final String TRAINER_LAST_NAME = "Castleberry";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 15);
    private static final int TRAINING_DURATION = 60;
    private static final long VALID_ID = 1L;

    private static final String AUTH_SUCCESS_MESSAGE = "Authentication successful!";


    public static AuthResponseDTO buildAuthResponseDTO() {
        return AuthResponseDTO.builder()
                .username(USERNAME)
                .message(AUTH_SUCCESS_MESSAGE)
                .build();
    }

    public static TrainingResponseDTO buildTrainingResponseDTO() {
        return TrainingResponseDTO.builder()
                .id(VALID_ID)
                .traineeUsername(USERNAME)
                .trainerUsername(TRAINER_USERNAME)
                .trainingName(TRAINING_NAME)
                .trainingTypeName(TRAINING_TYPE_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }

    public static TrainerResponseDTO buildTrainerResponseDTO() {
        return TrainerResponseDTO.builder()
                .userId(2L)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .username(TRAINER_USERNAME)
                .isActive(true)
                .build();
    }

    public static TraineeResponseDTO buildTraineeResponseDTO() {
        return TraineeResponseDTO.builder()
                .userId(VALID_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .isActive(true)
                .build();
    }
}
