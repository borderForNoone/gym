package org.gym.crm.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.dto.TrainerAssignmentUpdateDTO;
import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.mapper.TraineeMapper;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.User;
import org.gym.crm.search.criteria.TraineeTrainingCriteriaBuilder;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.service.common.UserInputValidator;
import org.gym.crm.util.CoreValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceImplTest {
    private static final String FIRST_NAME = "Owen";
    private static final String LAST_NAME = "Castleberry";
    private static final String USERNAME = "Owen.Castleberry";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String RAW_PASSWORD = "rawPassword";
    private static final long VALID_ID = 1L;
    private static final String TRAINEE_CANNOT_BE_NULL = "Trainee cannot be null";

    @Mock
    private TraineeDao dao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UserProfileService userCredentialGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TraineeTrainingCriteriaBuilder criteriaBuilder;
    @Mock
    private UserInputValidator userInputValidator;
    @Mock
    private TraineeMapper mapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainerService trainerService;
    @Spy
    private CoreValidator validator;

    @InjectMocks
    private TraineeServiceImpl service;

    private Trainee trainee;
    private Trainee savedTrainee;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        trainee = buildTrainee();

        savedTrainee = trainee.toBuilder()
                .id(VALID_ID)
                .user(trainee.getUser().toBuilder()
                        .id(VALID_ID)
                        .username(USERNAME)
                        .password(ENCODED_PASSWORD)
                        .isActive(true)
                        .build())
                .build();

        Logger logger = (Logger) LoggerFactory.getLogger(TraineeServiceImpl.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(TraineeServiceImpl.class);
        logger.detachAppender(logAppender);
    }

    @Test
    void createTrainee_shouldSaveTraineeWithCredentials() {
        when(userCredentialGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(userCredentialGenerator.generatePassword()).thenReturn(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(dao.save(any(Trainee.class))).thenReturn(savedTrainee);

        Trainee actual = service.create(trainee);

        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(ENCODED_PASSWORD, actual.getUser().getPassword());
        assertTrue(actual.getUser().getIsActive());
        verify(dao).save(any(Trainee.class));
    }

    @Test
    void createTrainee_shouldThrowException_whenTraineeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(null));

        assertEquals(TRAINEE_CANNOT_BE_NULL, exception.getMessage());
    }

    @Test
    void createTrainee_shouldLogInfo_whenCreatingTrainee() {
        when(userCredentialGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(userCredentialGenerator.generatePassword()).thenReturn(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(dao.save(any(Trainee.class))).thenReturn(savedTrainee);

        service.create(trainee);

        assertThat(logAppender.list)
                .filteredOn(log -> log.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains(FIRST_NAME) && message.contains(LAST_NAME))
                .anyMatch(message -> message.contains(USERNAME));
    }

    @Test
    void updateTrainersList_shouldReturnTrainerInfoDTOList() {
        TrainerAssignmentUpdateDTO dto = mock(TrainerAssignmentUpdateDTO.class);
        Trainer trainer1 = mock(Trainer.class);
        Trainer trainer2 = mock(Trainer.class);
        Trainee trainee = mock(Trainee.class);

        TrainerInfoDTO dto1 = TrainerInfoDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .username("trainer1")
                .isActive(true)
                .specialization("Yoga")
                .build();
        TrainerInfoDTO dto2 = TrainerInfoDTO.builder()
                .firstName("John2")
                .lastName("Doe2")
                .username("trainer2")
                .isActive(true)
                .specialization("Yoga")
                .build();

        when(dto.getTraineeUsername()).thenReturn("trainee1");
        when(dto.getTrainerUsernames()).thenReturn(List.of("trainer1", "trainer2"));
        when(trainee.getTrainers()).thenReturn(Set.of(trainer1, trainer2));
        when(trainerDao.findByUsername("trainer1")).thenReturn(Optional.of(trainer1));
        when(trainerDao.findByUsername("trainer2")).thenReturn(Optional.of(trainer2));
        when(dao.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
        doNothing().when(dao).updateTrainersList(anyString(), anyList());
        when(trainerMapper.toInfoDto(trainer1)).thenReturn(dto1);
        when(trainerMapper.toInfoDto(trainer2)).thenReturn(dto2);

        List<TrainerInfoDTO> result = service.updateTrainersList(dto);

        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
        verify(userInputValidator).validate(dto, "Trainer assignment");
        verify(trainerDao).findByUsername("trainer1");
        verify(trainerDao).findByUsername("trainer2");
        verify(dao).updateTrainersList(eq("trainee1"), anyList());
        verify(trainerMapper, times(2)).toInfoDto(any());
    }

    @Test
    void updateTrainee_shouldThrowException_whenTraineeIsNull() {
        assertThrows(Exception.class, () -> service.update(null));
    }

    @Test
    void updateTrainee_shouldUpdateTrainee_whenTraineeExists() {
        Trainee updatedData = savedTrainee.toBuilder()
                .address("new address")
                .build();

        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));
        when(dao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee actual = service.updateProfile(USERNAME, updatedData);

        assertEquals("new address", actual.getAddress());
        verify(dao).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldThrowException_whenTraineeNotFound() {
        Trainee updatedData = savedTrainee.toBuilder()
                .address("new address")
                .build();

        when(dao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateProfile(USERNAME, updatedData));
        verify(dao, never()).update(any());
    }

    @Test
    void updateProfile_shouldUpdateNameAndAddress() {
        Trainee updatedData = Trainee.builder()
                .user(User.builder()
                        .firstName("NewFirst")
                        .lastName("NewLast")
                        .isActive(true)
                        .build())
                .address("New Address")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .build();

        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = service.updateProfile(USERNAME, updatedData);

        assertEquals("NewFirst", result.getUser().getFirstName());
        assertEquals("NewLast", result.getUser().getLastName());
        assertEquals("New Address", result.getAddress());
        assertEquals(LocalDate.of(1995, 5, 15), result.getDateOfBirth());

        verify(dao).save(any());
    }

    @Test
    void updateProfile_shouldThrowEntityNotFound_whenTraineeNotFound() {
        Trainee updatedData = Trainee.builder()
                .user(User.builder().firstName("A").lastName("B").isActive(true).build())
                .build();

        when(dao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateProfile(USERNAME, updatedData));
    }

    @Test
    void updateProfile_shouldThrowException_whenFirstNameBlank() {
        Trainee updatedData = Trainee.builder()
                .user(User.builder().firstName("").lastName("B").isActive(true).build())
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(USERNAME, updatedData));
    }

    @Test
    void updateProfile_shouldThrowException_whenLastNameBlank() {
        Trainee updatedData = Trainee.builder()
                .user(User.builder().firstName("A").lastName("").isActive(true).build())
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(USERNAME, updatedData));
    }

    @Test
    void updateProfile_shouldThrowException_whenUsernameBlank() {
        Trainee trainee = Trainee.builder().build();

        assertThrows(IllegalArgumentException.class, () -> service.updateProfile("", trainee));
    }

    @Test
    void deleteTrainee_shouldDeleteTrainee_whenTraineeExists() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));

        service.deleteByUsername(USERNAME);

        verify(dao).delete(savedTrainee);
    }

    @Test
    void deleteTrainee_shouldThrowException_whenTraineeNotFound() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteByUsername(USERNAME));

        verify(dao, never()).deleteByUsername(any());
    }

    @Test
    void deleteByUsername_shouldDelete_whenTraineeExists() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));

        service.deleteByUsername(USERNAME);

        verify(dao).delete(savedTrainee);
    }

    @Test
    void deleteByUsername_shouldThrowEntityNotFound_whenTraineeNotFound() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteByUsername(USERNAME));
        verify(dao, never()).delete(any(Trainee.class));
    }

    @Test
    void deleteByUsername_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteByUsername(""));
    }

    @Test
    void authenticate_shouldReturnTrue_whenCredentialsMatch() {
        when(userCredentialGenerator.authenticate(USERNAME, ENCODED_PASSWORD)).thenReturn(true);

        boolean result = userCredentialGenerator.authenticate(USERNAME, ENCODED_PASSWORD);

        assertTrue(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenPasswordDoesNotMatch() {
        when(userCredentialGenerator.authenticate(USERNAME, "wrongPassword")).thenReturn(false);

        boolean result = userCredentialGenerator.authenticate(USERNAME, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenUsernameNotFound() {
        when(userCredentialGenerator.authenticate(USERNAME, ENCODED_PASSWORD)).thenReturn(false);

        boolean result = userCredentialGenerator.authenticate(USERNAME, ENCODED_PASSWORD);

        assertFalse(result);
    }

    @Test
    void changePassword_shouldUpdatePassword_whenOldPasswordMatches() throws Exception {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));
        when(passwordEncoder.matches("oldPassword", savedTrainee.getUser().getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        service.changePassword(USERNAME, "oldPassword", "newPassword");

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(dao).save(captor.capture());

        assertEquals("encodedNewPassword", captor.getValue().getUser().getPassword());
    }

    @Test
    void changePassword_shouldThrowAuthException_whenOldPasswordWrong() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));
        when(passwordEncoder.matches("wrongOld", savedTrainee.getUser().getPassword())).thenReturn(false);

        assertThrows(Exception.class,
                () -> service.changePassword(USERNAME, "wrongOld", "newPassword"));
        verify(dao, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowEntityNotFound_whenTraineeNotFound() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.changePassword(USERNAME, ENCODED_PASSWORD, "newPass"));
    }

    @Test
    void changePassword_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("", ENCODED_PASSWORD, "newPass"));
    }

    @Test
    void setActive_shouldDeactivate_whenCurrentlyActive() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));
        when(dao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = service.setActive(USERNAME, false);

        assertFalse(result.getUser().getIsActive());
        verify(dao).save(any());
    }

    @Test
    void setActive_shouldThrowIllegalState_whenAlreadySameStatus() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainee));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.setActive(USERNAME, true));

        assertThat(exception.getMessage()).contains("already active");
        verify(dao, never()).save(any());
    }

    @Test
    void setActive_shouldThrowEntityNotFound_whenTraineeNotFound() {
        when(dao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.setActive(USERNAME, false));
    }

    @Test
    void setActive_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.setActive("", false));
    }

    @Test
    void getUnassignedTrainers_shouldReturnList_whenTraineeExists() {
        Trainer trainer = buildTrainer();
        when(dao.existsByUsername(USERNAME)).thenReturn(true);
        when(dao.findUnassignedTrainers(USERNAME)).thenReturn(List.of(trainer));

        List<Trainer> result = service.getUnassignedTrainers(USERNAME);

        assertEquals(1, result.size());
        verify(dao).findUnassignedTrainers(USERNAME);
    }

    @Test
    void getUnassignedTrainers_shouldThrowEntityNotFound_whenTraineeNotFound() {
        when(dao.existsByUsername(USERNAME)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.getUnassignedTrainers(USERNAME));
        verify(dao, never()).findUnassignedTrainers(any());
    }

    @Test
    void getUnassignedTrainers_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.getUnassignedTrainers("  "));
    }

    @Test
    void getTrainings_shouldThrowException_whenFilterIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getTrainings(null));

        assertThat(exception.getMessage()).contains("Filter");
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .user(User.builder()
                        .id(VALID_ID)
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .isActive(true)
                        .build())
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("123 Main St")
                .build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .id(2L)
                .user(User.builder()
                        .id(2L)
                        .firstName("John")
                        .lastName("Smith")
                        .username("John.Smith")
                        .password("pass")
                        .isActive(true)
                        .build())
                .build();
    }
}