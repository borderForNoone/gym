package com.gym.crm.service.impl;

import com.gym.crm.facade.dto.PasswordChangeRequest;
import com.gym.crm.facade.dto.ToggleActiveRequestDTO;
import com.gym.crm.exception.BadCredentialsException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.service.common.UserInputValidator;
import com.gym.crm.service.common.CoreValidator;
import jakarta.persistence.EntityNotFoundException;
import org.gym.crm.rest.LoginRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private CoreValidator validator;
    @Mock
    private UserInputValidator userInputValidator;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileServiceImpl service;

    @Test
    void authenticate_shouldReturnTrue_whenPasswordValid() {
        User user = User.builder().username("user").password("hash").isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        Boolean result = service.authenticate("user", "pass");

        assertThat(result).isTrue();
    }

    @Test
    void authenticate_shouldReturnFalse_whenUserNotFound() {
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.empty());
        when(trainerRepository.findByUser_Username("user")).thenReturn(Optional.empty());

        Boolean result = service.authenticate("user", "pass");

        assertThat(result).isFalse();
    }

    @Test
    void login_shouldReturnUser() {
        LoginRequest request = mock(LoginRequest.class);
        User user = User.builder().username("user").password("hash").isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(request.getUsername()).thenReturn("user");
        when(request.getPassword()).thenReturn("pass");
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        User result = service.login(request);

        assertThat(result).isNotNull();
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        LoginRequest request = mock(LoginRequest.class);

        User user = User.builder().username("user").password("hash").isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(request.getUsername()).thenReturn("user");
        when(request.getPassword()).thenReturn("bad");
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(request)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void changePassword_shouldUpdatePassword_forTrainee() {
        PasswordChangeRequest request = mock(PasswordChangeRequest.class);

        User user = User.builder().username("user").password("hash").isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(request.getUsername()).thenReturn("user");
        when(request.getOldPassword()).thenReturn("old");
        when(request.getNewPassword()).thenReturn("new");
        when(traineeRepository.existsByUser_Username("user")).thenReturn(true);
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encoded");

        service.changePassword(request);

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordWrong() {
        PasswordChangeRequest request = PasswordChangeRequest.builder().username("user").oldPassword("bad").newPassword("new").build();
        User user = User.builder().username("user").password("hash").isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(request)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void toggleActive_shouldToggleTrainee() {
        ToggleActiveRequestDTO request = mock(ToggleActiveRequestDTO.class);
        User user = User.builder().username("user").password("hash").isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(request.getUsername()).thenReturn("user");
        when(traineeRepository.existsByUser_Username("user")).thenReturn(true);
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));

        service.toggleActive(request);

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void generateUsername_shouldReturnBase_whenFree() {
        when(traineeRepository.existsByUser_Username("tom.tomas")).thenReturn(false);
        when(trainerRepository.existsByUser_Username("tom.tomas")).thenReturn(false);

        String result = service.generateUsername("tom", "tomas");

        assertThat(result).isEqualTo("tom.tomas");
    }

    @Test
    void generatePassword_shouldReturn10Chars() {
        String result = service.generatePassword();

        assertThat(result).hasSize(10);
    }

    @Test
    void generateUsername_shouldAddSuffix_whenUsernameTaken() {
        when(traineeRepository.existsByUser_Username(anyString())).thenReturn(false);

        when(trainerRepository.existsByUser_Username(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);

            return switch (username) {
                case "tom.tomas" -> true;
                case "tom.tomas1" -> false;
                default -> false;
            };
        });

        String result = service.generateUsername("tom", "tomas");

        assertThat(result).isEqualTo("tom.tomas1");
    }

    @Test
    void toggleActive_shouldToggleTrainer_whenExists() {
        ToggleActiveRequestDTO request = mock(ToggleActiveRequestDTO.class);
        User user = User.builder().username("trainer1").password("hash").isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(request.getUsername()).thenReturn("trainer1");
        when(traineeRepository.existsByUser_Username("trainer1")).thenReturn(false);
        when(trainerRepository.existsByUser_Username("trainer1")).thenReturn(true);
        when(trainerRepository.findByUser_Username("trainer1")).thenReturn(Optional.of(trainer));

        service.toggleActive(request);

        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void toggleActive_shouldThrow_whenUserNotFound() {
        ToggleActiveRequestDTO request = mock(ToggleActiveRequestDTO.class);

        when(request.getUsername()).thenReturn("unknown");
        when(traineeRepository.existsByUser_Username("unknown")).thenReturn(false);
        when(trainerRepository.existsByUser_Username("unknown")).thenReturn(false);

        assertThatThrownBy(() -> service.toggleActive(request))
                .isInstanceOf(com.gym.crm.exception.EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}