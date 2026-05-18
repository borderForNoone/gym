package org.gym.crm.service.impl;

import org.gym.crm.dao.TraineeDao;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.dto.common.PasswordChangeRequest;
import org.gym.crm.exception.BadCredentialsException;
import org.gym.crm.exception.CoreValidationException;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.User;
import org.gym.crm.rest.LoginRequest;
import org.gym.crm.util.CoreValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.gym.crm.util.TestConstants.ALLOWED_CHARS;
import static org.gym.crm.util.TestConstants.FIRST_NAME;
import static org.gym.crm.util.TestConstants.LAST_NAME;
import static org.gym.crm.util.TestConstants.PASSWORD_LENGTH;
import static org.gym.crm.util.TestConstants.USERNAME;
import static org.gym.crm.util.TestConstants.USERNAME_WITH_SUFFIX_1;
import static org.gym.crm.util.TestConstants.USERNAME_WITH_SUFFIX_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String ENCODED_NEW_PASSWORD = "encodedNewPassword";

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private CoreValidator validator;
    @InjectMocks
    private UserProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
    }

    @Test
    void generateUsername_shouldReturnBaseUsername_whenNoDuplicates() {
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(false);
        when(trainerDao.existsByUsername(USERNAME)).thenReturn(false);

        String actual = service.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(USERNAME, actual);
    }

    @Test
    void generateUsername_shouldReturnUsernameWithSuffix1_whenBaseExists() {
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(true);
        when(traineeDao.existsByUsername(USERNAME_WITH_SUFFIX_1)).thenReturn(false);
        when(trainerDao.existsByUsername(USERNAME_WITH_SUFFIX_1)).thenReturn(false);

        String actual = service.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(USERNAME_WITH_SUFFIX_1, actual);
    }

    @Test
    void generateUsername_shouldReturnUsernameWithSuffix2_whenBaseAndSuffix1Exist() {
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(true);
        when(traineeDao.existsByUsername(USERNAME_WITH_SUFFIX_1)).thenReturn(false);
        when(trainerDao.existsByUsername(USERNAME_WITH_SUFFIX_1)).thenReturn(true);
        when(traineeDao.existsByUsername(USERNAME_WITH_SUFFIX_2)).thenReturn(false);
        when(trainerDao.existsByUsername(USERNAME_WITH_SUFFIX_2)).thenReturn(false);

        String actual = service.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(USERNAME_WITH_SUFFIX_2, actual);
    }

    @Test
    void generateUsername_shouldReturnSuffix2_whenSuffix1Taken() {
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(true);
        when(traineeDao.existsByUsername(USERNAME_WITH_SUFFIX_1)).thenReturn(true);
        when(traineeDao.existsByUsername(USERNAME_WITH_SUFFIX_2)).thenReturn(false);

        String actual = service.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(USERNAME_WITH_SUFFIX_2, actual);
    }

    @Test
    void generateUsername_shouldThrowException_whenFirstNameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generateUsername("", LAST_NAME));
    }

    @Test
    void generateUsername_shouldThrowException_whenLastNameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generateUsername(FIRST_NAME, ""));
    }

    @Test
    void generateUsername_shouldThrowException_whenFirstNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generateUsername(null, LAST_NAME));
    }

    @Test
    void generateUsername_shouldThrowException_whenLastNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generateUsername(FIRST_NAME, null));
    }

    @Test
    void generateUsername_shouldThrowUsernameTooLongException_whenBaseExceedsLimit() {
        String longFirst = "A".repeat(60);
        String longLast = "B".repeat(60);

        assertThrows(CoreValidationException.class,
                () -> service.generateUsername(longFirst, longLast));
    }

    @Test
    void generatePassword_shouldReturn10CharString() {
        String actual = service.generatePassword();

        assertNotNull(actual);
        assertEquals(PASSWORD_LENGTH, actual.length());
    }

    @Test
    void generatePassword_shouldContainOnlyAllowedChars() {
        String actual = service.generatePassword();

        assertTrue(actual.chars().allMatch(c -> ALLOWED_CHARS.indexOf(c) >= 0));
    }

    @Test
    void generatePassword_shouldReturnDifferentPasswordsEachTime() {
        String first = service.generatePassword();
        String second = service.generatePassword();

        assertNotEquals(first, second);
    }

    @Test
    void authenticate_shouldReturnTrue_whenTraineeExistsAndPasswordMatches() {
        Trainee trainee = buildTrainee(buildUser());

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        assertTrue(service.authenticate(USERNAME, PASSWORD));
    }

    @Test
    void authenticate_shouldReturnFalse_whenTraineeExistsButPasswordDoesNotMatch() {
        Trainee trainee = buildTrainee(buildUser());

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertFalse(service.authenticate(USERNAME, PASSWORD));
    }

    @Test
    void authenticate_shouldReturnTrue_whenTrainerExistsAndPasswordMatches() {
        Trainer trainer = buildTrainer(buildUser());

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        assertTrue(service.authenticate(USERNAME, PASSWORD));
    }

    @Test
    void authenticate_shouldReturnFalse_whenNeitherTraineeNorTrainerExists() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(service.authenticate(USERNAME, PASSWORD));
    }

    @Test
    void authenticate_shouldThrowException_whenUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.authenticate("", PASSWORD));
    }

    @Test
    void authenticate_shouldThrowException_whenPasswordIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.authenticate(USERNAME, ""));
    }

    @Test
    void authenticate_shouldThrowException_whenUsernameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.authenticate(null, PASSWORD));
    }

    @Test
    void authenticate_shouldThrowException_whenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.authenticate(USERNAME, null));
    }

    @Test
    void login_shouldReturnUser_whenTraineeExistsAndPasswordMatches() {
        User expectedUser = buildUser();
        Trainee trainee = buildTrainee(expectedUser);
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        User actual = service.login(request);

        assertEquals(expectedUser, actual);
    }

    @Test
    void login_shouldReturnUser_whenTrainerExistsAndPasswordMatches() {
        User expectedUser = buildUser();
        Trainer trainer = buildTrainer(expectedUser);
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        User actual = service.login(request);

        assertEquals(expectedUser, actual);
    }

    @Test
    void login_shouldThrowEntityNotFoundException_whenUserNotFound() {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.login(request));
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenPasswordDoesNotMatch() {
        User user = buildUser();
        Trainee trainee = buildTrainee(user);
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> service.login(request));
    }

    @Test
    void login_shouldThrowException_whenUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.login(new LoginRequest("", PASSWORD)));
    }

    @Test
    void login_shouldThrowException_whenPasswordIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.login(new LoginRequest(USERNAME, "")));
    }

    @Test
    void changePassword_shouldUpdatePassword_whenTraineeExists() {
        Trainee trainee = buildTrainee(buildUser());

        doNothing().when(validator).validate(any(PasswordChangeRequest.class), anyString());
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        service.changePassword(buildPasswordChangeRequest());

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao).update(captor.capture());
        assertEquals(ENCODED_NEW_PASSWORD, captor.getValue().getUser().getPassword());
    }

    @Test
    void changePassword_shouldUpdatePassword_whenTrainerExists() {
        Trainer trainer = buildTrainer(buildUser());

        doNothing().when(validator).validate(any(PasswordChangeRequest.class), anyString());
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(false);
        when(trainerDao.existsByUsername(USERNAME)).thenReturn(true);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        service.changePassword(buildPasswordChangeRequest());

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).update(captor.capture());
        assertEquals(ENCODED_NEW_PASSWORD, captor.getValue().getUser().getPassword());
    }

    @Test
    void changePassword_shouldThrowEntityNotFoundException_whenUserNotFound() {
        doNothing().when(validator).validate(any(PasswordChangeRequest.class), anyString());
        when(traineeDao.existsByUsername(USERNAME)).thenReturn(false);
        when(trainerDao.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        assertThrows(EntityNotFoundException.class,
                () -> service.changePassword(buildPasswordChangeRequest()));
    }

    @Test
    void changePassword_shouldThrowException_whenRequestIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(null));
    }

    private User buildUser() {
        return User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .isActive(true)
                .build();
    }

    private Trainee buildTrainee(User user) {
        return Trainee.builder()
                .user(user)
                .build();
    }

    private Trainer buildTrainer(User user) {
        return Trainer.builder()
                .user(user)
                .build();
    }

    private PasswordChangeRequest buildPasswordChangeRequest() {
        return PasswordChangeRequest.builder()
                .username(USERNAME)
                .oldPassword(PASSWORD)
                .newPassword(NEW_PASSWORD)
                .build();
    }
}