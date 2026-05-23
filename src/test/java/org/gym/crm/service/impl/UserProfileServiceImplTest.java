package org.gym.crm.service.impl;

import org.gym.crm.config.TransactionManager;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.dto.PasswordChangeRequest;
import org.gym.crm.dto.ToggleActiveRequestDTO;
import org.gym.crm.exception.BadCredentialsException;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.User;
import org.gym.crm.rest.LoginRequest;
import org.gym.crm.service.common.UserInputValidator;
import org.gym.crm.util.CoreValidator;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {
    private static final String USERNAME = "tom.tomas";
    private static final String PASSWORD = "pass";
    private static final String ENCODED = "encoded";

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private CoreValidator validator;
    @Mock
    private UserInputValidator userInputValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private Session session;

    private UserProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserProfileServiceImpl(traineeDao, trainerDao, validator, userInputValidator, passwordEncoder, transactionManager);
    }

    @Test
    void authenticate_shouldReturnTrue_whenTraineePasswordMatches() {
        User user = User.builder()
                .password(ENCODED)
                .build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED)).thenReturn(true);

        Boolean result = service.authenticate(USERNAME, PASSWORD);

        assertTrue(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenNoUserFound() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        Boolean result = service.authenticate(USERNAME, PASSWORD);

        assertFalse(result);
    }

    @Test
    void login_shouldReturnUser_whenCredentialsCorrect() {
        User user = User.builder()
                .username(USERNAME)
                .password(ENCODED)
                .build();
        Trainee trainee = Trainee.builder().user(user).build();
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED)).thenReturn(true);

        User result = service.login(request);

        assertEquals(user, result);
        verify(passwordEncoder).matches(PASSWORD, ENCODED);
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {
        User user = User.builder().password(ENCODED).build();
        Trainee trainee = Trainee.builder().user(user).build();
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> service.login(request));
    }

    @Test
    void changePassword_shouldCallUpdate_whenValid() {
        User user = User.builder().password(ENCODED).build();
        Trainee trainee = Trainee.builder().user(user).build();
        PasswordChangeRequest request = PasswordChangeRequest.builder().username(USERNAME).oldPassword(PASSWORD).newPassword("newPassword123").build();

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED)).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNew");
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        doAnswer(inv -> {
            Consumer<Session> c = inv.getArgument(0);
            c.accept(session);
            return null;
        }).when(transactionManager).performWithinTx(any());

        assertDoesNotThrow(() -> service.changePassword(request));
        verify(traineeDao).update(any());
        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordIncorrect() {
        User user = User.builder().password(ENCODED).build();
        Trainee trainee = Trainee.builder().user(user).build();
        PasswordChangeRequest request = PasswordChangeRequest.builder().username(USERNAME).oldPassword("wrong").newPassword("newPassword123").build();

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("wrong", ENCODED)).thenReturn(false);
        doAnswer(inv -> {
            Consumer<Session> c = inv.getArgument(0);
            c.accept(session);
            return null;
        }).when(transactionManager).performWithinTx(any());

        assertThrows(BadCredentialsException.class, () -> service.changePassword(request));
    }

    @Test
    void toggleActive_shouldDeactivateTrainee() {
        User user = User.builder().isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();
        ToggleActiveRequestDTO request = ToggleActiveRequestDTO.builder().username(USERNAME).isActive(false).build();

        doNothing().when(userInputValidator).validate(any(), any());
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        doAnswer(inv -> {
            Consumer<Session> c = inv.getArgument(0);
            c.accept(session);
            return null;
        }).when(transactionManager).performWithinTx(any());

        assertDoesNotThrow(() -> service.toggleActive(request));
        verify(traineeDao).update(argThat(updated -> !updated.getUser().getIsActive()));
    }

    @Test
    void toggleActive_shouldThrow_whenUserNotFound() {
        ToggleActiveRequestDTO request = ToggleActiveRequestDTO.builder().username(USERNAME).isActive(false).build();

        doNothing().when(userInputValidator).validate(any(), any());
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(false);
        when(trainerDao.existsByUsername(USERNAME)).thenReturn(false);
        doAnswer(inv -> {
            Consumer<Session> c = inv.getArgument(0);
            c.accept(session);
            return null;
        }).when(transactionManager).performWithinTx(any());

        assertThrows(EntityNotFoundException.class, () -> service.toggleActive(request));
    }

    @Test
    void generatePassword_shouldReturn10Chars() {
        String password = service.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }
}