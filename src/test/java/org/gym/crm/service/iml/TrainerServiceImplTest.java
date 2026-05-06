package org.gym.crm.service.iml;

import org.gym.crm.dao.TrainerDao;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.service.impl.TrainerServiceImpl;
import org.gym.crm.util.CoreValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private CoreValidator validator;
    @InjectMocks
    private TrainerServiceImpl service;

    private Trainer trainer;
    private Trainer savedTrainer;

    @BeforeEach
    void setUp() {
        trainer = buildTrainer();
        savedTrainer = trainer.toBuilder()
                .id(ID)
                .user(trainer.getUser().toBuilder()
                        .id(ID)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .isActive(true)
                        .build())
                .build();
    }

    @Test
    void create_shouldGenerateUsernameAndPasswordAndSave() {
        when(userProfileService.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(userProfileService.generatePassword()).thenReturn(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encodedPassword");

        Trainer expected = trainer.toBuilder()
                .user(trainer.getUser().toBuilder()
                        .username(USERNAME)
                        .password("encodedPassword")
                        .build())
                .build();

        when(trainerDao.save(expected)).thenReturn(expected);

        Trainer actual = service.create(trainer);

        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals("encodedPassword", actual.getUser().getPassword());

        verify(userProfileService).generateUsername(FIRST_NAME, LAST_NAME);
        verify(userProfileService).generatePassword();
        verify(passwordEncoder).encode(PASSWORD);
        verify(trainerDao).save(expected);
    }

    @Test
    void findById_shouldReturnTrainer_whenExists() {
        when(trainerDao.findById(ID)).thenReturn(Optional.of(trainer));

        Optional<Trainer> actual = service.findById(ID);

        assertTrue(actual.isPresent());
        assertEquals(trainer, actual.get());
        verify(trainerDao).findById(ID);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(trainerDao.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        Optional<Trainer> actual = service.findById(NON_EXISTING_ID);

        assertTrue(actual.isEmpty());
        verify(trainerDao).findById(NON_EXISTING_ID);
    }

    @Test
    void findAll_shouldReturnAllTrainers() {
        List<Trainer> expected = List.of(trainer);
        when(trainerDao.findAll()).thenReturn(expected);

        List<Trainer> actual = service.findAll();

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.getFirst(), actual.getFirst());
        verify(trainerDao).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoTrainers() {
        when(trainerDao.findAll()).thenReturn(List.of());

        List<Trainer> actual = service.findAll();

        assertTrue(actual.isEmpty());
        verify(trainerDao).findAll();
    }

    @Test
    void update_shouldUpdateAndReturnTrainer() {
        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer actual = service.update(trainer);

        assertEquals(trainer, actual);
        verify(trainerDao).update(trainer);
    }

    @Test
    void authenticate_shouldReturnTrue_whenCredentialsMatch() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        boolean result = service.authenticate(USERNAME, PASSWORD);

        assertTrue(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenPasswordWrong() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        boolean result = service.authenticate(USERNAME, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenUsernameNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        boolean result = service.authenticate(USERNAME, PASSWORD);

        assertFalse(result);
    }

    @Test
    void authenticate_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("", PASSWORD));
    }

    @Test
    void authenticate_shouldThrowException_whenPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate(USERNAME, ""));
    }

    @Test
    void findByUsername_shouldReturnTrainer_whenExists() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        Optional<Trainer> result = service.findByUsername(USERNAME);

        assertTrue(result.isPresent());
        assertEquals(savedTrainer, result.get());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<Trainer> result = service.findByUsername(USERNAME);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findByUsername("  "));
    }

    @Test
    void changePassword_shouldUpdate_whenOldPasswordMatches() throws Exception {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(trainerDao.save(any())).thenReturn(savedTrainer);

        service.changePassword(USERNAME, PASSWORD, "newPassword");

        assertEquals("newPassword", savedTrainer.getUser().getPassword());
        verify(trainerDao).save(savedTrainer);
    }

    @Test
    void changePassword_shouldThrowAuthException_whenOldPasswordWrong() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        assertThrows(Exception.class,
                () -> service.changePassword(USERNAME, "wrongOld", "newPassword"));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowEntityNotFound_whenTrainerNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.changePassword(USERNAME, PASSWORD, "newPass"));
    }

    @Test
    void changePassword_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("", PASSWORD, "newPass"));
    }

    @Test
    void changePassword_shouldThrowException_whenOldPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(USERNAME, "", "newPass"));
    }

    @Test
    void changePassword_shouldThrowException_whenNewPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(USERNAME, PASSWORD, ""));
    }

    @Test
    void updateProfile_shouldUpdateNameAndSpecialization() {
        TrainingType newType = TrainingType.builder().trainingTypeName("YOGA").build();
        Trainer updatedData = Trainer.builder()
                .user(User.builder().firstName("NewFirst").lastName("NewLast").build())
                .specialization(newType)
                .build();

        String newUsername = "NewFirst.NewLast";
        Trainer expectedSaved = savedTrainer.toBuilder()
                .user(savedTrainer.getUser().toBuilder()
                        .firstName("NewFirst")
                        .lastName("NewLast")
                        .username(newUsername)
                        .build())
                .specialization(newType)
                .build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(userProfileService.generateUsername("NewFirst", "NewLast")).thenReturn(newUsername);
        when(trainerDao.update(any())).thenReturn(expectedSaved);

        Trainer actual = service.updateProfile(USERNAME, updatedData);

        assertEquals("NewFirst", actual.getUser().getFirstName());
        assertEquals("NewLast", actual.getUser().getLastName());
        assertEquals(newUsername, actual.getUser().getUsername());
        assertEquals("YOGA", actual.getSpecialization().getTrainingTypeName());
        verify(trainerDao).update(any());
    }

    @Test
    void updateProfile_shouldThrowEntityNotFound_whenTrainerNotFound() {
        Trainer updatedData = Trainer.builder()
                .user(User.builder().firstName("A").lastName("B").build())
                .build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateProfile(USERNAME, updatedData));
    }

    @Test
    void updateProfile_shouldThrowException_whenFirstNameBlank() {
        Trainer updatedData = Trainer.builder()
                .user(User.builder().firstName("").lastName("B").build())
                .build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile(USERNAME, updatedData));
    }

    @Test
    void updateProfile_shouldThrowException_whenLastNameBlank() {
        Trainer updatedData = Trainer.builder()
                .user(User.builder().firstName("A").lastName("").build())
                .build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile(USERNAME, updatedData));
    }

    @Test
    void updateProfile_shouldThrowException_whenUsernameBlank() {
        Trainer trainer = Trainer.builder().build();

        assertThrows(IllegalArgumentException.class, () -> service.updateProfile("", trainer));
    }

    @Test
    void setActive_shouldDeactivate_whenCurrentlyActive() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(trainerDao.save(any())).thenReturn(savedTrainer);

        service.setActive(USERNAME, false);

        assertFalse(savedTrainer.getUser().getIsActive());
        verify(trainerDao).save(savedTrainer);
    }

    @Test
    void setActive_shouldActivate_whenCurrentlyInactive() {
        Trainer inactiveTrainer = savedTrainer.toBuilder()
                .user(savedTrainer.getUser().toBuilder().isActive(false).build())
                .build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(inactiveTrainer));
        when(trainerDao.save(any())).thenReturn(inactiveTrainer);

        service.setActive(USERNAME, true);

        assertTrue(inactiveTrainer.getUser().getIsActive());
        verify(trainerDao).save(inactiveTrainer);
    }

    @Test
    void setActive_shouldThrowIllegalState_whenAlreadySameStatus() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.setActive(USERNAME, true));

        assertTrue(exception.getMessage().contains("already active"));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void setActive_shouldThrowEntityNotFound_whenTrainerNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.setActive(USERNAME, false));
    }

    @Test
    void setActive_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.setActive("", false));
    }

    private TrainingType buildFitnessType() {
        return TrainingType.builder().trainingTypeName(FITNESS).build();
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