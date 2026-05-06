package org.gym.crm.service.iml;

import org.gym.crm.dao.TraineeDao;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.exception.UsernameTooLongException;
import org.gym.crm.service.impl.UserProfileServiceImpl;
import org.gym.crm.util.CoreValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.gym.crm.util.TestConstants.ALLOWED_CHARS;
import static org.gym.crm.util.TestConstants.FIRST_NAME;
import static org.gym.crm.util.TestConstants.LAST_NAME;
import static org.gym.crm.util.TestConstants.PASSWORD_LENGTH;
import static org.gym.crm.util.TestConstants.USERNAME;
import static org.gym.crm.util.TestConstants.USERNAME_WITH_SUFFIX_1;
import static org.gym.crm.util.TestConstants.USERNAME_WITH_SUFFIX_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Spy
    private CoreValidator validator;
    @InjectMocks
    private UserProfileServiceImpl service;

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

        assertThrows(UsernameTooLongException.class,
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
}