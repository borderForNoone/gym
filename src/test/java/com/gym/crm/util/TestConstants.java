package com.gym.crm.util;

import java.time.LocalDate;

public class TestConstants {
    private TestConstants() {}

    public static final String FIRST_NAME = "Kaden";
    public static final String LAST_NAME = "Voss";
    public static final String USERNAME = "Kaden.Voss";
    public static final String USERNAME_WITH_SUFFIX_1 = "Kaden.Voss1";
    public static final String USERNAME_WITH_SUFFIX_2 = "Kaden.Voss2";
    public static final String PASSWORD = "testpass1";
    public static final int PASSWORD_LENGTH = 10;
    public static final String ADDRESS = "10 Sheep St";
    public static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    public static final String TRAINEE_NOT_FOUND_MESSAGE = "Trainee not found with id: ";

    public static final String SECOND_FIRST_NAME = "Liora";
    public static final String SECOND_LAST_NAME = "Nash";
    public static final String SECOND_USERNAME = "Liora.Nash";
    public static final String SECOND_PASSWORD = "testpass2";
    public static final String SECOND_ADDRESS = "456 Oak Ave";
    public static final LocalDate SECOND_DOB = LocalDate.of(1995, 5, 5);

    public static final String TRAINER_FIRST_NAME = "Mira";
    public static final String TRAINER_LAST_NAME = "Calder";
    public static final String TRAINER_USERNAME = "Mira.Calder";
    public static final String NOT_FOUND_MESSAGE = "Trainer not found with id: ";

    public static final String TRAINING_NAME = "Morning Workout";
    public static final String FITNESS = "FITNESS";
    public static final String CARDIO = "CARDIO";
    public static final String YOGA = "YOGA";
    public static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 1);
    public static final int DURATION = 60;

    public static final Long ID = 1L;
    public static final Long SECOND_ID = 2L;
    public static final Long THIRD_ID = 3L;
    public static final Long NON_EXISTING_ID = 99L;

    public static final String TRAINEES_FILE = "trainees.csv";
    public static final String TRAINERS_FILE = "trainers.csv";
    public static final String TRAININGS_FILE = "trainings.csv";

    public static final String ALLOWED_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
}
