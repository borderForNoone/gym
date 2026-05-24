package com.gym.crm.service.impl;

import com.gym.crm.config.TransactionManager;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.dto.CreatedTrainer;
import com.gym.crm.dto.TrainerInfoDTO;
import com.gym.crm.dto.TrainerRequestDTO;
import com.gym.crm.dto.TrainerResponseDTO;
import com.gym.crm.dto.TrainerUpdateDTO;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.UserInputValidator;
import com.gym.crm.util.CoreValidator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    private static final String USERNAME = "tom.tomas";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String FIRST_NAME = "Tom";
    private static final String LAST_NAME = "Tomas";
    private static final String SPECIALIZATION = "Fitness";

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private TrainerTrainingCriteriaBuilder criteriaBuilder;

    @Mock
    private CoreValidator validator;

    @Mock
    private UserInputValidator userInputValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TrainerMapper mapper;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Session session;

    @Mock
    private CriteriaBuilder criteriaBuilderMock;

    @Mock
    private CriteriaQuery<Training> criteriaQuery;

    @Mock
    private Query<Training> query;

    private TrainerServiceImpl trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl(trainerDao, trainingTypeDao, userProfileService, criteriaBuilder, validator, userInputValidator, passwordEncoder, mapper,
                transactionManager);
    }

    @Test
    void createTrainer_shouldCreateTrainerSuccessfully() {
        TrainerRequestDTO request = TrainerRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).specialization(SPECIALIZATION).build();
        User user = User.builder().build();
        Trainer trainer = Trainer.builder().user(user).build();
        Trainer savedTrainer = mock(Trainer.class, RETURNS_DEEP_STUBS);
        TrainingType trainingType = TrainingType.builder().trainingTypeName(SPECIALIZATION).build();

        when(mapper.toEntity(request)).thenReturn(trainer);
        when(userProfileService.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(userProfileService.generatePassword()).thenReturn(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeDao.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.of(trainingType));
        when(transactionManager.performReturningWithinTx(any())).thenAnswer(invocation -> {
            Function<Session, Object> function = invocation.getArgument(0);
            return function.apply(session);
        });
        when(trainerDao.save(any())).thenReturn(savedTrainer);

        CreatedTrainer result = trainerService.createTrainer(request);

        assertThat(result.trainer()).isEqualTo(savedTrainer);
        assertThat(result.rawPassword()).isEqualTo(PASSWORD);
        verify(userInputValidator).validate(request, "TRAINER");
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void createTrainer_shouldThrowException_whenTrainingTypeNotFound() {
        TrainerRequestDTO request = TrainerRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).specialization(SPECIALIZATION).build();
        Trainer trainer = mock(Trainer.class, RETURNS_DEEP_STUBS);

        when(mapper.toEntity(request)).thenReturn(trainer);
        when(trainingTypeDao.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.empty());
        when(transactionManager.performReturningWithinTx(any())).thenAnswer(invocation -> {
            Function<Session, Object> function = invocation.getArgument(0);
            return function.apply(session);
        });

        assertThrows(EntityNotFoundException.class, () -> trainerService.createTrainer(request));
    }

    @Test
    void updateTrainer_shouldUpdateSuccessfully() {
        TrainerUpdateDTO request = TrainerUpdateDTO.builder().username(USERNAME).firstName(FIRST_NAME).lastName(LAST_NAME).specialization(SPECIALIZATION).isActive(true).build();
        User existingUser = User.builder().username(USERNAME).firstName("Old").lastName("Name").isActive(false).build();
        Trainer existingTrainer = Trainer.builder().user(existingUser).build();
        Trainer updatedTrainer = Trainer.builder().user(existingUser).build();
        TrainingType trainingType = TrainingType.builder().trainingTypeName(SPECIALIZATION).build();
        TrainerResponseDTO responseDTO = TrainerResponseDTO.builder().username(USERNAME).build();

        when(transactionManager.performReturningWithinTx(any())).thenAnswer(invocation -> {
            Function<Session, Object> function = invocation.getArgument(0);
            return function.apply(session);
        });
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainingTypeDao.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.of(trainingType));
        when(trainerDao.update(any())).thenReturn(updatedTrainer);
        when(mapper.toDto(updatedTrainer)).thenReturn(responseDTO);

        TrainerResponseDTO result = trainerService.updateTrainer(request);

        assertEquals(responseDTO, result);
        verify(userInputValidator).validate(request, "TRAINER");
        verify(trainerDao).update(any(Trainer.class));
    }

    @Test
    void getTrainerByUsername_shouldReturnTrainerInfo() {
        Trainer trainer = mock(Trainer.class);
        TrainerInfoDTO infoDTO = TrainerInfoDTO.builder().username(USERNAME).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(mapper.toInfoDto(trainer)).thenReturn(infoDTO);

        TrainerInfoDTO result = trainerService.getTrainerByUsername(USERNAME);

        assertEquals(infoDTO, result);
        verify(userInputValidator).validateUsername(USERNAME);
        verify(mapper).toInfoDto(trainer);
    }

    @Test
    void getTrainerByUsername_shouldThrowException_whenTrainerNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.getTrainerByUsername(USERNAME));
    }

    @Test
    void changePassword_shouldChangePasswordSuccessfully() {
        User user = User.builder().username(USERNAME).password(ENCODED_PASSWORD).build();

        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        doAnswer(invocation -> {
            Consumer<Session> consumer = invocation.getArgument(0);
            consumer.accept(session);
            return null;
        }).when(transactionManager).performWithinTx(any());

        assertDoesNotThrow(() -> trainerService.changePassword(USERNAME, PASSWORD, "newPassword"));
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void changePassword_shouldThrowException_whenPasswordIncorrect() {
        User user = User.builder().username(USERNAME).password(ENCODED_PASSWORD).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);
        doAnswer(invocation -> {
            Consumer<Session> consumer = invocation.getArgument(0);
            consumer.accept(session);

            return null;
        }).when(transactionManager).performWithinTx(any());

        assertThrows(RuntimeException.class, () -> trainerService.changePassword(USERNAME, PASSWORD, "newPassword"));
    }

    @Test
    void updateProfile_shouldUpdateProfileSuccessfully() {
        User existingUser = User.builder().firstName("Old").lastName("Name").build();
        Trainer existingTrainer = Trainer.builder().user(existingUser).build();
        User updatedUser = User.builder().firstName(FIRST_NAME).lastName(LAST_NAME).build();
        TrainingType specialization = TrainingType.builder().trainingTypeName(SPECIALIZATION).build();
        Trainer updatedData = Trainer.builder().user(updatedUser).specialization(specialization).build();
        Trainer savedTrainer = Trainer.builder().user(updatedUser).specialization(specialization).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainerDao.update(any())).thenReturn(savedTrainer);
        when(transactionManager.performReturningWithinTx(any())).thenAnswer(invocation -> {
            Function<Session, Object> function = invocation.getArgument(0);
            return function.apply(session);
        });

        Trainer result = trainerService.updateProfile(USERNAME, updatedData);

        assertEquals(savedTrainer, result);
        verify(trainerDao).update(any(Trainer.class));
    }

    @Test
    void setActive_shouldUpdateActiveStatus() {
        User user = User.builder().isActive(false).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        doAnswer(invocation -> {
            Consumer<Session> consumer = invocation.getArgument(0);
            consumer.accept(session);
            return null;
        }).when(transactionManager).performWithinTx(any());

        trainerService.setActive(USERNAME, true);

        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void setActive_shouldThrowException_whenAlreadyActive() {
        User user = User.builder().isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        doAnswer(invocation -> {
            Consumer<Session> consumer = invocation.getArgument(0);
            consumer.accept(session);

            return null;
        }).when(transactionManager).performWithinTx(any());

        assertThrows(IllegalStateException.class, () -> trainerService.setActive(USERNAME, true));
    }

    @Test
    void getTrainings_shouldReturnTrainingsList() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().username(USERNAME).build();

        Session mockSession = mock(Session.class, RETURNS_DEEP_STUBS);
        Training training = mock(Training.class);
        CriteriaQuery<Training> mockCriteriaQuery = mock(CriteriaQuery.class);
        HibernateCriteriaBuilder hibCriteriaBuilder = mock(HibernateCriteriaBuilder.class);

        when(transactionManager.performReturningWithinTx(any())).thenAnswer(invocation -> {
            Function<Session, Object> function = invocation.getArgument(0);
            return function.apply(mockSession);
        });

        when(mockSession.getCriteriaBuilder()).thenReturn(hibCriteriaBuilder);
        when(criteriaBuilder.build(hibCriteriaBuilder, filter)).thenReturn(mockCriteriaQuery);
        when(mockSession.createQuery(mockCriteriaQuery).getResultList()).thenReturn(List.of(training));

        List<Training> result = trainerService.getTrainings(filter);

        assertThat(result).hasSize(1).containsExactly(training);
        verify(criteriaBuilder).build(hibCriteriaBuilder, filter);
    }

    @Test
    void getNotAssignedToTrainee_shouldReturnTrainerInfoList() {
        Trainer trainer = mock(Trainer.class);
        TrainerInfoDTO infoDTO = TrainerInfoDTO.builder().username(USERNAME).build();

        when(trainerDao.findNotAssignedToTrainee(USERNAME)).thenReturn(List.of(trainer));
        when(mapper.toInfoDto(trainer)).thenReturn(infoDTO);

        List<TrainerInfoDTO> result = trainerService.getNotAssignedToTrainee(USERNAME);

        assertEquals(1, result.size());
        assertEquals(infoDTO, result.getFirst());
        verify(userInputValidator).validateUsername(USERNAME);
    }

    @Test
    void updateProfile_shouldUpdateOnlySpecialization_whenUserIsNull() {
        User existingUser = User.builder().firstName("Old").lastName("Name").build();
        Trainer existingTrainer = Trainer.builder().user(existingUser).build();
        TrainingType specialization = TrainingType.builder().trainingTypeName(SPECIALIZATION).build();
        Trainer updatedData = Trainer.builder().specialization(specialization).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainerDao.update(any())).thenReturn(existingTrainer);
        when(transactionManager.performReturningWithinTx(any())).thenAnswer(invocation -> {
            Function<Session, Object> function = invocation.getArgument(0);

            return function.apply(session);
        });

        trainerService.updateProfile(USERNAME, updatedData);
        verify(trainerDao).update(any(Trainer.class));
    }

    @Test
    void changePassword_shouldThrowException_whenTrainerNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Consumer<Session> consumer = invocation.getArgument(0);
            consumer.accept(session);
            return null;
        }).when(transactionManager).performWithinTx(any());

        assertThrows(EntityNotFoundException.class, () -> trainerService.changePassword(USERNAME, PASSWORD, "newPassword"));
    }

    @Test
    void setActive_shouldThrowException_whenTrainerNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Consumer<Session> consumer = invocation.getArgument(0);
            consumer.accept(session);

            return null;
        }).when(transactionManager).performWithinTx(any());

        assertThrows(EntityNotFoundException.class, () -> trainerService.setActive(USERNAME, true));
    }
}