package com.gym.crm.service.impl;

import com.gym.crm.exception.BadCredentialsException;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.facade.dto.AuthResponseDTO;
import com.gym.crm.facade.dto.PasswordChangeRequest;
import com.gym.crm.facade.dto.ToggleActiveRequestDTO;
import com.gym.crm.model.FieldName;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.security.JwtService;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.CoreValidator;
import com.gym.crm.service.common.UserInputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.rest.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private static final String USERNAME_LABEL = "Username";
    private static final String PASSWORD_LABEL = "Password";
    private static final String USER_NOT_FOUND_BY_USERNAME = "User not found by username: %s";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final CoreValidator validator;
    private final UserInputValidator userInputValidator;
    private final PasswordEncoder passwordEncoder;
    private final CoreValidator coreValidator;
    private final JwtService jwtService;

    @Transactional
    @Override
    public String generateUsername(String firstName, String lastName) {
        coreValidator.validateNotBlank(firstName, "First name");
        coreValidator.validateNotBlank(lastName, "Last name");

        String baseUsername = firstName.trim() + "." + lastName.trim();
        validator.validateTextFieldSize(baseUsername, FieldName.USERNAME, 110);

        if (!isUsernameTaken(baseUsername)) {
            log.debug("Generated username='{}'", baseUsername);
            return baseUsername;
        }

        long suffix = 1;
        String candidate;
        do {
            candidate = baseUsername + suffix;
            suffix++;
        } while (isUsernameTaken(candidate));

        log.debug("Generated username='{}' with suffix due to duplicates", candidate);
        return candidate;
    }

    @Transactional()
    @Override
    public String generatePassword() {
        return IntStream.range(0, PASSWORD_LENGTH)
                .mapToObj(i -> String.valueOf(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length()))))
                .collect(Collectors.joining());
    }

    @Transactional
    @Override
    public AuthResponseDTO authenticate(String username, String password) {
        coreValidator.validateNotBlank(username, USERNAME_LABEL);
        coreValidator.validateNotBlank(password, PASSWORD_LABEL);

        User user = traineeRepository.findByUser_Username(username)
                .map(Trainee::getUser)
                .or(() -> trainerRepository.findByUser_Username(username).map(Trainer::getUser))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials for user: " + username);
        }

        String token = jwtService.generateToken(username);
        return AuthResponseDTO.builder().username(username).token(token).build();
    }

    @Transactional
    @Override
    public void changePassword(@Valid PasswordChangeRequest request) {
        validator.validate(request, "Password change request");

        String username = request.getUsername();
        log.info("Changing password for user: username={}", username);

        User user = traineeRepository.findByUser_Username(username)
                .map(Trainee::getUser)
                .or(() -> trainerRepository.findByUser_Username(username).map(Trainer::getUser))
                .orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        String encoded = passwordEncoder.encode(request.getNewPassword());

        if (traineeRepository.existsByUser_Username(username)) {
            Trainee trainee = traineeRepository.findByUser_Username(username).get();
            traineeRepository.save(trainee.toBuilder().user(user.toBuilder().password(encoded).build()).build());
        } else {
            Trainer trainer = trainerRepository.findByUser_Username(username).get();
            trainerRepository.save(trainer.toBuilder().user(user.toBuilder().password(encoded).build()).build());
        }

        log.info("Changed password for user: username={}", username);
    }

    @Transactional()
    @Override
    public User login(LoginRequest request) {
        coreValidator.validateNotBlank(request.getUsername(), USERNAME_LABEL);
        coreValidator.validateNotBlank(request.getPassword(), PASSWORD_LABEL);

        String username = request.getUsername();

        User user = traineeRepository.findByUser_Username(username)
                .map(Trainee::getUser).or(() -> trainerRepository.findByUser_Username(username).map(Trainer::getUser))
                .orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password for username: " + username);
        }

        log.info("User logged in: username={}", username);
        return user;
    }

    @Transactional
    @Override
    public void toggleActive(@Valid ToggleActiveRequestDTO request) {
        userInputValidator.validate(request, "Toggle active request");

        String username = request.getUsername();
        log.info("Changing active status for user: username={}", username);

        if (traineeRepository.existsByUser_Username(username)) {
            Trainee trainee = traineeRepository.findByUser_Username(username).orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

            boolean current = trainee.getUser().getIsActive();
            traineeRepository.save(trainee.toBuilder().user(trainee.getUser().toBuilder().isActive(!current).build()).build());

            log.info("Trainee {}: username={}", current ? "deactivated" : "activated", username);
            return;
        }

        if (trainerRepository.existsByUser_Username(username)) {
            Trainer trainer = trainerRepository.findByUser_Username(username).orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

            boolean current = trainer.getUser().getIsActive();

            trainerRepository.save(trainer.toBuilder().user(trainer.getUser().toBuilder().isActive(!current).build()).build());

            log.info("Trainer {}: username={}", current ? "deactivated" : "activated", username);
            return;
        }

        throw new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username));
    }

    private boolean isUsernameTaken(String username) {
        return traineeRepository.existsByUser_Username(username) || trainerRepository.existsByUser_Username(username);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
