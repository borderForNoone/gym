package org.gym.crm.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.dao.TrainingTypeDao;
import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainerUpdateDTO;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import org.gym.crm.search.filter.TrainerTrainingFilter;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "John.Smith";
    private static final String RAW_PASSWORD = "rawPassword";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String SPECIALIZATION = "Yoga";
    private static final long VALID_ID = 1L;

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private TrainingTypeDao trainingTypeDAO;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private TrainerTrainingCriteriaBuilder criteriaBuilder;
    @Mock
    private UserInputValidator userInputValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TrainerMapper mapper;
    @Mock
    private EntityManager entityManager;
    @Spy
    private CoreValidator validator;

    @InjectMocks
    private TrainerServiceImpl service;

    private Trainer trainer;
    private Trainer savedTrainer;
    private TrainingType trainingType;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        trainingType = TrainingType.builder()
                .id(1L)
                .trainingTypeName(SPECIALIZATION)
                .build();

        trainer = buildTrainer(null, null, null);

        savedTrainer = trainer.toBuilder()
                .id(VALID_ID)
                .user(trainer.getUser().toBuilder()
                        .id(VALID_ID)
                        .username(USERNAME)
                        .password(ENCODED_PASSWORD)
                        .isActive(true)
                        .build())
                .specialization(trainingType)
                .build();

        Logger logger = (Logger) LoggerFactory.getLogger(TrainerServiceImpl.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        ReflectionTestUtils.setField(service, "entityManager", entityManager);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(TrainerServiceImpl.class);
        logger.detachAppender(logAppender);
    }

    @Test
    void createTrainer_shouldSaveTrainerWithCredentials() {
        TrainerRequestDTO request = buildRequest();
        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(userProfileService.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(userProfileService.generatePassword()).thenReturn(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeDAO.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.of(trainingType));
        when(mapper.toEntity(request)).thenReturn(trainer);
        when(trainerDao.save(any(Trainer.class))).thenReturn(savedTrainer);
        TrainerResponseDTO responseDTO = TrainerResponseDTO.builder().build();
        when(mapper.toDto(savedTrainer)).thenReturn(responseDTO);

        TrainerResponseDTO result = service.createTrainer(request);

        assertNotNull(result);
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        Trainer captured = captor.getValue();
        assertEquals(USERNAME, captured.getUser().getUsername());
        assertEquals(ENCODED_PASSWORD, captured.getUser().getPassword());
    }

    @Test
    void createTrainer_shouldLogInfo_whenCreatingTrainer() {
        TrainerRequestDTO request = buildRequest();
        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(userProfileService.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(userProfileService.generatePassword()).thenReturn(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeDAO.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.of(trainingType));
        when(mapper.toEntity(request)).thenReturn(trainer);
        when(trainerDao.save(any())).thenReturn(savedTrainer);
        when(mapper.toDto(savedTrainer)).thenReturn(TrainerResponseDTO.builder().build());

        service.createTrainer(request);

        assertThat(logAppender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains(FIRST_NAME) && msg.contains(LAST_NAME));
    }

    @Test
    void createTrainer_shouldThrowEntityNotFound_whenTrainingTypeNotFound() {
        TrainerRequestDTO request = buildRequest();

        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(trainingTypeDAO.findByTrainingTypeName(SPECIALIZATION))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.createTrainer(request));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void updateTrainer_shouldReturnUpdatedTrainer_whenValid() {
        TrainerUpdateDTO request = buildUpdateRequest();
        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(trainingTypeDAO.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.of(trainingType));
        when(trainerDao.update(any(Trainer.class))).thenReturn(savedTrainer);
        TrainerResponseDTO responseDTO = TrainerResponseDTO.builder().build();
        when(mapper.toDto(savedTrainer)).thenReturn(responseDTO);

        TrainerResponseDTO result = service.updateTrainer(request);

        assertNotNull(result);
        verify(trainerDao).update(any(Trainer.class));
    }

    @Test
    void updateTrainer_shouldThrowEntityNotFound_whenTrainerNotFound() {
        TrainerUpdateDTO request = buildUpdateRequest();
        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(request));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void updateTrainer_shouldThrowEntityNotFound_whenTrainingTypeNotFound() {
        TrainerUpdateDTO request = buildUpdateRequest();
        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(trainingTypeDAO.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(request));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void getTrainerByUsername_shouldReturnInfoDTO_whenTrainerExists() {
        doNothing().when(userInputValidator).validateUsername(USERNAME);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        TrainerInfoDTO infoDTO = TrainerInfoDTO.builder().username(USERNAME).build();
        when(mapper.toInfoDto(savedTrainer)).thenReturn(infoDTO);

        TrainerInfoDTO result = service.getTrainerByUsername(USERNAME);

        assertNotNull(result);
        verify(trainerDao).findByUsername(USERNAME);
    }

    @Test
    void getTrainerByUsername_shouldThrowEntityNotFound_whenTrainerNotFound() {
        doNothing().when(userInputValidator).validateUsername(USERNAME);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getTrainerByUsername(USERNAME));
    }

    @Test
    void changePassword_shouldUpdatePassword_whenOldPasswordMatches() throws Exception {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(passwordEncoder.matches("oldPassword", ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNew");

        service.changePassword(USERNAME, "oldPassword", "newPassword");

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        assertEquals("encodedNew", captor.getValue().getUser().getPassword());
    }

    @Test
    void changePassword_shouldThrowAuthException_whenOldPasswordWrong() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(passwordEncoder.matches("wrongOld", ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(Exception.class,
                () -> service.changePassword(USERNAME, "wrongOld", "newPassword"));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowEntityNotFound_whenTrainerNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.changePassword(USERNAME, "old", "new"));
    }

    @Test
    void changePassword_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("", "old", "new"));
    }

    @Test
    void changePassword_shouldThrowException_whenOldPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(USERNAME, "", "new"));
    }

    @Test
    void changePassword_shouldThrowException_whenNewPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(USERNAME, "old", ""));
    }

    @Test
    void updateProfile_shouldUpdateNameAndSpecialization() {
        Trainer updatedData = buildTrainer("NewFirst", "NewLast", trainingType);

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));
        when(userProfileService.generateUsername("NewFirst", "NewLast")).thenReturn("NewFirst.NewLast");
        when(trainerDao.update(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.updateProfile(USERNAME, updatedData);

        assertEquals("NewFirst", result.getUser().getFirstName());
        assertEquals("NewLast", result.getUser().getLastName());
        assertEquals(trainingType, result.getSpecialization());
        verify(trainerDao).update(any(Trainer.class));
    }

    @Test
    void updateProfile_shouldThrowEntityNotFound_whenTrainerNotFound() {
        TrainingType specialization = TrainingType.builder()
                .id(1L)
                .trainingTypeName("Strength")
                .build();

        Trainer updatedData = buildTrainer("A", "B", specialization);

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateProfile(USERNAME, updatedData));

        verify(trainerDao, never()).update(any());
    }

    @Test
    void updateProfile_shouldThrowException_whenUsernameBlank() {
        Trainer updatedData = buildTrainer("A", "B", null);

        assertThrows(IllegalArgumentException.class, () -> service.updateProfile("", updatedData));
    }

    @Test
    void updateProfile_shouldThrowException_whenTrainerDataNull() {
        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(USERNAME, null));
    }

    @Test
    void setActive_shouldDeactivate_whenCurrentlyActive() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        service.setActive(USERNAME, false);

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        assertFalse(captor.getValue().getUser().getIsActive());
    }

    @Test
    void setActive_shouldThrowIllegalState_whenAlreadySameStatus() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(savedTrainer));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.setActive(USERNAME, true));

        assertThat(ex.getMessage()).contains("already");
        verify(trainerDao, never()).save(any());
    }

    @Test
    void setActive_shouldThrowEntityNotFound_whenTrainerNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.setActive(USERNAME, false));
    }

    @Test
    void setActive_shouldThrowException_whenUsernameBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.setActive("", false));
    }

    @Test
    void getTrainings_shouldReturnList_whenFilterValid() {
        TrainerTrainingFilter filter = mock(TrainerTrainingFilter.class);
        CriteriaBuilder criteriaBuilderMock = mock(CriteriaBuilder.class);
        CriteriaQuery<Training> criteriaQuery = mock(CriteriaQuery.class);
        TypedQuery<Training> typedQuery = mock(TypedQuery.class);
        List<Training> expectedTrainings = List.of(mock(Training.class), mock(Training.class));

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilderMock);
        when(criteriaBuilder.build(criteriaBuilderMock, filter)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedTrainings);

        List<Training> result = service.getTrainings(filter);

        assertNotNull(result);
        assertEquals(expectedTrainings, result);
        verify(validator).validateNotNull(filter, "Filter");
        verify(criteriaBuilder).build(criteriaBuilderMock, filter);
        verify(entityManager).createQuery(criteriaQuery);
        verify(typedQuery).getResultList();
    }

    @Test
    void getTrainings_shouldThrowException_whenFilterIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.getTrainings(null));

        assertThat(ex.getMessage()).contains("Filter");
    }

    @Test
    void getNotAssignedToTrainee_shouldReturnList_whenTraineeExists() {
        doNothing().when(userInputValidator).validateUsername("trainee.user");
        when(trainerDao.findNotAssignedToTrainee("trainee.user")).thenReturn(List.of(savedTrainer));
        TrainerInfoDTO infoDTO = TrainerInfoDTO.builder().username(USERNAME).build();
        when(mapper.toInfoDto(savedTrainer)).thenReturn(infoDTO);

        List<TrainerInfoDTO> result = service.getNotAssignedToTrainee("trainee.user");

        assertEquals(1, result.size());
        verify(trainerDao).findNotAssignedToTrainee("trainee.user");
    }

    private Trainer buildTrainer(String firstName, String lastName, TrainingType specialization) {
        return Trainer.builder()
                .user(User.builder()
                        .firstName(firstName != null ? firstName : FIRST_NAME)
                        .lastName(lastName != null ? lastName : LAST_NAME)
                        .isActive(true)
                        .build())
                .specialization(specialization)
                .build();
    }

    private TrainerRequestDTO buildRequest() {
        return TrainerRequestDTO.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .specialization(SPECIALIZATION)
                .build();
    }

    private TrainerUpdateDTO buildUpdateRequest() {
        return TrainerUpdateDTO.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .specialization(SPECIALIZATION)
                .isActive(true)
                .build();
    }
}