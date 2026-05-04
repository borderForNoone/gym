package org.gym.crm.storage.parser;

import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.gym.crm.util.TestConstants.ADDRESS;
import static org.gym.crm.util.TestConstants.CARDIO;
import static org.gym.crm.util.TestConstants.FIRST_NAME;
import static org.gym.crm.util.TestConstants.FITNESS;
import static org.gym.crm.util.TestConstants.ID;
import static org.gym.crm.util.TestConstants.LAST_NAME;
import static org.gym.crm.util.TestConstants.PASSWORD;
import static org.gym.crm.util.TestConstants.SECOND_ID;
import static org.gym.crm.util.TestConstants.THIRD_ID;
import static org.gym.crm.util.TestConstants.TRAINER_FIRST_NAME;
import static org.gym.crm.util.TestConstants.TRAINER_LAST_NAME;
import static org.gym.crm.util.TestConstants.TRAINER_USERNAME;
import static org.gym.crm.util.TestConstants.TRAINING_NAME;
import static org.gym.crm.util.TestConstants.USERNAME;
import static org.gym.crm.util.TestConstants.YOGA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvParserTest {
    private CsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvParser();
    }

    @Test
    void parseTrainee_shouldParseAllFields() {
        String[] fields = buildTraineeFields("true", "1990-01-15");

        Trainee actual = parser.parseTrainee(fields);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());
        assertTrue(actual.getUser().getIsActive());
        assertEquals(LocalDate.of(1990, 1, 15), actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());
        assertEquals(Long.parseLong(ID.toString()), actual.getId());
    }

    @Test
    void parseTrainee_shouldSetDateOfBirthNull_whenBlank() {
        String[] fields = buildTraineeFields("true", "");

        Trainee actual = parser.parseTrainee(fields);

        assertNull(actual.getDateOfBirth());
    }

    @Test
    void parseTrainee_shouldParseIsActiveFalse() {
        String[] fields = buildTraineeFields("false", "1990-01-15");

        Trainee actual = parser.parseTrainee(fields);

        assertFalse(actual.getUser().getIsActive());
    }

    @Test
    void parseTrainer_shouldParseAllFields() {
        String[] fields = buildTrainerFields("true", FITNESS);

        Trainer actual = parser.parseTrainer(fields);

        assertEquals(TRAINER_FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(TRAINER_LAST_NAME, actual.getUser().getLastName());
        assertEquals(TRAINER_USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());
        assertTrue(actual.getUser().getIsActive());
        assertEquals(FITNESS, actual.getSpecialization().getTrainingTypeName());
        assertEquals(Long.parseLong(SECOND_ID.toString()), actual.getId());
    }

    @Test
    void parseTrainer_shouldParseIsActiveFalse() {
        String[] fields = buildTrainerFields("false", FITNESS);

        Trainer actual = parser.parseTrainer(fields);

        assertFalse(actual.getUser().getIsActive());
    }

    @Test
    void parseTrainer_shouldParseSpecialization() {
        String[] fields = buildTrainerFields("true", YOGA);

        Trainer actual = parser.parseTrainer(fields);

        assertEquals(YOGA, actual.getSpecialization().getTrainingTypeName());
    }

    @Test
    void parseTraining_shouldParseAllFields() {
        String[] fields = buildTrainingFields(TRAINING_NAME, FITNESS, "2024-03-15", "90");

        Training actual = parser.parseTraining(fields);

        assertEquals(1L, actual.getId());
        assertEquals(2L, actual.getTrainee().getId());
        assertEquals(3L, actual.getTrainer().getId());
        assertEquals(TRAINING_NAME, actual.getTrainingName());
        assertEquals(FITNESS, actual.getTrainingType().getTrainingTypeName());
        assertEquals(LocalDate.of(2024, 3, 15), actual.getTrainingDate());
        assertEquals(90, actual.getTrainingDuration());
    }

    @Test
    void parseTraining_shouldParseTrainingDuration() {
        String[] fields = buildTrainingFields("Evening Run", CARDIO, "2024-06-01", "45");

        Training actual = parser.parseTraining(fields);

        assertEquals(45, actual.getTrainingDuration());
    }

    @Test
    void parseTraining_shouldParseTrainingType() {
        String[] fields = buildTrainingFields("Yoga Session", YOGA, "2024-06-01", "60");

        Training actual = parser.parseTraining(fields);

        assertEquals(YOGA, actual.getTrainingType().getTrainingTypeName());
    }

    private String[] buildTraineeFields(String isActive, String dateOfBirth) {
        return new String[]{
                ID.toString(),
                FIRST_NAME,
                LAST_NAME,
                USERNAME,
                PASSWORD,
                isActive,
                dateOfBirth,
                ADDRESS,
                ID.toString()
        };
    }

    private String[] buildTrainerFields(String isActive, String specialization) {
        return new String[]{
                ID.toString(),
                TRAINER_FIRST_NAME,
                TRAINER_LAST_NAME,
                TRAINER_USERNAME,
                PASSWORD,
                isActive,
                specialization,
                SECOND_ID.toString()
        };
    }

    private String[] buildTrainingFields(String name, String type, String date, String duration) {
        return new String[]{
                ID.toString(),
                SECOND_ID.toString(),
                THIRD_ID.toString(),
                name,
                type,
                date,
                duration
        };
    }
}