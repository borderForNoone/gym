package org.gym.crm.service;

import org.gym.crm.dao.TrainerDao;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.gym.crm.util.TestConstants.FIRST_NAME;
import static org.gym.crm.util.TestConstants.FITNESS;
import static org.gym.crm.util.TestConstants.ID;
import static org.gym.crm.util.TestConstants.LAST_NAME;
import static org.gym.crm.util.TestConstants.NON_EXISTING_ID;
import static org.gym.crm.util.TestConstants.PASSWORD;
import static org.gym.crm.util.TestConstants.TRAINER_USERNAME;
import static org.gym.crm.util.TestConstants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UserProfileService userProfileService;
    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainer = buildTrainer();
    }

    @Test
    void create_shouldGenerateUsernameAndPasswordAndSave() {
        when(userProfileService.generateUsername(FIRST_NAME, LAST_NAME))
                .thenReturn(USERNAME);
        when(userProfileService.generatePassword())
                .thenReturn(PASSWORD);

        Trainer expected = trainer.toBuilder()
                .user(trainer.getUser().toBuilder()
                        .username(USERNAME)
                        .password(PASSWORD)
                        .build())
                .build();

        when(trainerDao.save(expected)).thenReturn(expected);

        Trainer actual = trainerService.create(trainer);

        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());

        verify(userProfileService).generateUsername(FIRST_NAME, LAST_NAME);
        verify(userProfileService).generatePassword();
        verify(trainerDao).save(expected);
    }

    @Test
    void findById_shouldReturnTrainer_whenExists() {
        when(trainerDao.findById(ID)).thenReturn(Optional.of(trainer));

        Optional<Trainer> actual = trainerService.findById(ID);

        assertTrue(actual.isPresent());
        assertEquals(trainer, actual.get());
        verify(trainerDao).findById(ID);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(trainerDao.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        Optional<Trainer> actual = trainerService.findById(NON_EXISTING_ID);

        assertTrue(actual.isEmpty());
        verify(trainerDao).findById(NON_EXISTING_ID);
    }

    @Test
    void findAll_shouldReturnAllTrainers() {
        List<Trainer> expected = List.of(trainer);
        when(trainerDao.findAll()).thenReturn(expected);

        List<Trainer> actual = trainerService.findAll();

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.getFirst(), actual.getFirst());
        verify(trainerDao).findAll();
    }

    @Test
    void update_shouldUpdateAndReturnTrainer() {
        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer actual = trainerService.update(trainer);

        assertEquals(trainer, actual);
        verify(trainerDao).update(trainer);
    }

    private TrainingType buildFitnessType() {
        return TrainingType.builder()
                .trainingTypeName(FITNESS)
                .build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .user(User.builder()
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .username(TRAINER_USERNAME)
                        .isActive(true)
                        .build())
                .specialization(buildFitnessType())
                .build();
    }
}