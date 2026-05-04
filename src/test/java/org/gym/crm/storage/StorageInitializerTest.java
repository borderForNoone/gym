package org.gym.crm.storage;

import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.storage.parser.CsvParser;
import org.gym.crm.storage.reader.CsvDataReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gym.crm.util.TestConstants.ADDRESS;
import static org.gym.crm.util.TestConstants.DATE_OF_BIRTH;
import static org.gym.crm.util.TestConstants.DURATION;
import static org.gym.crm.util.TestConstants.FIRST_NAME;
import static org.gym.crm.util.TestConstants.FITNESS;
import static org.gym.crm.util.TestConstants.ID;
import static org.gym.crm.util.TestConstants.LAST_NAME;
import static org.gym.crm.util.TestConstants.PASSWORD;
import static org.gym.crm.util.TestConstants.SECOND_ADDRESS;
import static org.gym.crm.util.TestConstants.SECOND_DOB;
import static org.gym.crm.util.TestConstants.SECOND_FIRST_NAME;
import static org.gym.crm.util.TestConstants.SECOND_LAST_NAME;
import static org.gym.crm.util.TestConstants.SECOND_PASSWORD;
import static org.gym.crm.util.TestConstants.SECOND_USERNAME;
import static org.gym.crm.util.TestConstants.TRAINEES_FILE;
import static org.gym.crm.util.TestConstants.TRAINERS_FILE;
import static org.gym.crm.util.TestConstants.TRAINER_FIRST_NAME;
import static org.gym.crm.util.TestConstants.TRAINER_LAST_NAME;
import static org.gym.crm.util.TestConstants.TRAINER_USERNAME;
import static org.gym.crm.util.TestConstants.TRAININGS_FILE;
import static org.gym.crm.util.TestConstants.TRAINING_DATE;
import static org.gym.crm.util.TestConstants.TRAINING_NAME;
import static org.gym.crm.util.TestConstants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageInitializerTest {
    @Mock
    private CsvDataReader csvDataReader;
    @Mock
    private CsvParser csvParser;
    @Mock
    private Storage storage;
    @Mock
    private TraineeStorage traineeStorage;
    @Mock
    private TrainerStorage trainerStorage;
    @Mock
    private TrainingStorage trainingStorage;
    @InjectMocks
    private StorageInitializer storageInitializer;

    private Map<Long, Trainee> trainees;
    private Map<Long, Trainer> trainers;
    private Map<Long, Training> trainings;
    private Trainee trainee;
    private Trainer trainer;
    private Training training;

    @BeforeEach
    void setUp() {
        trainees = new HashMap<>();
        trainers = new HashMap<>();
        trainings = new HashMap<>();
        trainee = buildTrainee();
        trainer = buildTrainer();
        training = buildTraining();

        storageInitializer.setTraineesFilePath(TRAINEES_FILE);
        storageInitializer.setTrainersFilePath(TRAINERS_FILE);
        storageInitializer.setTrainingsFilePath(TRAININGS_FILE);

        when(storage.getTraineeStorage()).thenReturn(traineeStorage);
        when(storage.getTrainerStorage()).thenReturn(trainerStorage);
        when(storage.getTrainingStorage()).thenReturn(trainingStorage);
        when(traineeStorage.getTrainees()).thenReturn(trainees);
        when(trainerStorage.getTrainers()).thenReturn(trainers);
        when(trainingStorage.getTrainings()).thenReturn(trainings);
    }

    @Test
    void init_shouldLoadTrainees() {
        List<String[]> traineeData = new ArrayList<>();
        traineeData.add(new String[]{
                String.valueOf(ID),
                FIRST_NAME,
                LAST_NAME,
                USERNAME,
                PASSWORD,
                "true",
                DATE_OF_BIRTH.toString(),
                ADDRESS
        });

        when(csvDataReader.readData(TRAINEES_FILE)).thenReturn(traineeData);
        when(csvDataReader.readData(TRAINERS_FILE)).thenReturn(new ArrayList<>());
        when(csvDataReader.readData(TRAININGS_FILE)).thenReturn(new ArrayList<>());
        when(csvParser.parseTrainee(traineeData.getFirst())).thenReturn(trainee);

        storageInitializer.init();

        assertEquals(1, trainees.size());
        assertEquals(trainee, trainees.get(1L));
        verify(csvParser).parseTrainee(traineeData.getFirst());
    }

    @Test
    void init_shouldLoadTrainers() {
        List<String[]> trainerData = new ArrayList<>();
        trainerData.add(new String[]{
                String.valueOf(ID),
                TRAINER_FIRST_NAME,
                TRAINER_LAST_NAME,
                TRAINER_USERNAME,
                PASSWORD,
                "true",
                FITNESS
        });

        when(csvDataReader.readData(TRAINEES_FILE)).thenReturn(new ArrayList<>());
        when(csvDataReader.readData(TRAINERS_FILE)).thenReturn(trainerData);
        when(csvDataReader.readData(TRAININGS_FILE)).thenReturn(new ArrayList<>());
        when(csvParser.parseTrainer(trainerData.getFirst())).thenReturn(trainer);

        storageInitializer.init();

        assertEquals(1, trainers.size());
        assertEquals(trainer, trainers.get(1L));
        verify(csvParser).parseTrainer(trainerData.getFirst());
    }

    @Test
    void init_shouldLoadTrainings() {
        List<String[]> trainingData = new ArrayList<>();
        trainingData.add(new String[]{
                String.valueOf(ID),
                String.valueOf(ID),
                String.valueOf(ID),
                TRAINING_NAME,
                FITNESS,
                TRAINING_DATE.toString(),
                String.valueOf(DURATION)
        });

        when(csvDataReader.readData(TRAINEES_FILE)).thenReturn(new ArrayList<>());
        when(csvDataReader.readData(TRAINERS_FILE)).thenReturn(new ArrayList<>());
        when(csvDataReader.readData(TRAININGS_FILE)).thenReturn(trainingData);
        when(csvParser.parseTraining(trainingData.getFirst())).thenReturn(training);

        storageInitializer.init();

        assertEquals(1, trainings.size());
        assertEquals(training, trainings.get(1L));
        verify(csvParser).parseTraining(trainingData.getFirst());
    }

    @Test
    void init_shouldHandleEmptyFiles() {
        when(csvDataReader.readData(TRAINEES_FILE)).thenReturn(List.of());
        when(csvDataReader.readData(TRAINERS_FILE)).thenReturn(List.of());
        when(csvDataReader.readData(TRAININGS_FILE)).thenReturn(List.of());

        storageInitializer.init();

        assertTrue(trainees.isEmpty());
        assertTrue(trainers.isEmpty());
        assertTrue(trainings.isEmpty());
    }

    @Test
    void init_shouldLoadMultipleTrainees() {
        Trainee secondTrainee = buildSecondTrainee();
        String[] firstFields = buildTraineeFields(ID, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, DATE_OF_BIRTH, ADDRESS);
        String[] secondFields = buildTraineeFields(2L, SECOND_FIRST_NAME, SECOND_LAST_NAME, SECOND_USERNAME, SECOND_PASSWORD, SECOND_DOB, SECOND_ADDRESS);

        when(csvDataReader.readData(TRAINEES_FILE)).thenReturn(List.of(firstFields, secondFields));
        when(csvDataReader.readData(TRAINERS_FILE)).thenReturn(List.of());
        when(csvDataReader.readData(TRAININGS_FILE)).thenReturn(List.of());
        when(csvParser.parseTrainee(firstFields)).thenReturn(trainee);
        when(csvParser.parseTrainee(secondFields)).thenReturn(secondTrainee);

        storageInitializer.init();

        assertEquals(2, trainees.size());
        assertEquals(trainee, trainees.get(1L));
        assertEquals(secondTrainee, trainees.get(2L));
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .user(User.builder()
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .username(USERNAME)
                        .isActive(true)
                        .build())
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .user(User.builder()
                        .firstName(TRAINER_FIRST_NAME)
                        .lastName(TRAINER_LAST_NAME)
                        .username(TRAINER_USERNAME)
                        .isActive(true)
                        .build())
                .specialization(buildFitnessType())
                .build();
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

    private Trainee buildSecondTrainee() {
        return Trainee.builder()
                .user(User.builder()
                        .firstName(SECOND_FIRST_NAME)
                        .lastName(SECOND_LAST_NAME)
                        .username(SECOND_USERNAME)
                        .isActive(true)
                        .build())
                .build();
    }

    private String[] buildTraineeFields(Long id, String firstName, String lastName,
                                        String username, String password,
                                        LocalDate dateOfBirth, String address) {
        return new String[]{
                String.valueOf(id),
                firstName,
                lastName,
                username,
                password,
                "true",
                dateOfBirth.toString(),
                address
        };
    }
}