package com.gym.crm.service.impl;

import com.gym.crm.config.TransactionManager;
import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.ToggleActiveRequestDTO;
import com.gym.crm.exception.BadCredentialsException;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.model.FieldName;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.UserInputValidator;
import com.gym.crm.util.CoreValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.rest.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final CoreValidator validator;
    private final UserInputValidator userInputValidator;
    private final PasswordEncoder passwordEncoder;
    private final TransactionManager transactionManager;

    @Override
    public String generateUsername(String firstName, String lastName) {
        validator.validateNotBlank(firstName, "First name");
        validator.validateNotBlank(lastName, "Last name");

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

    @Override
    public String generatePassword() {
        return IntStream.range(0, PASSWORD_LENGTH)
                .mapToObj(i -> String.valueOf(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length()))))
                .collect(Collectors.joining());
    }

    @Override
    public Boolean authenticate(String username, String password) {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateNotBlank(password, PASSWORD_LABEL);

        return traineeDao.findByUsername(username)
                .<User>map(Trainee::getUser)
                .or(() -> trainerDao.findByUsername(username).map(Trainer::getUser))
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    @Override
    public void changePassword(@Valid PasswordChangeRequest request) {
        validator.validate(request, "Password change request");

        String username = request.getUsername();
        log.info("Changing password for user: username={}", username);

        transactionManager.performWithinTx(session -> {
            User user = traineeDao.findByUsername(username).<User>map(Trainee::getUser).or(() -> trainerDao.findByUsername(username).map(Trainer::getUser))
                    .orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BadCredentialsException("Old password is incorrect");
            }

            updatePassword(username, passwordEncoder.encode(request.getNewPassword()));
        });

        log.info("Changed password for user: username={}", username);
    }

    @Override
    public User login(LoginRequest request) {
        validator.validateNotBlank(request.getUsername(), USERNAME_LABEL);
        validator.validateNotBlank(request.getPassword(), PASSWORD_LABEL);

        String username = request.getUsername();

        User user = traineeDao.findByUsername(username)
                .<User>map(Trainee::getUser)
                .or(() -> trainerDao.findByUsername(username).map(Trainer::getUser))
                .orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password for username: " + username);
        }

        log.info("User logged in: username={}", username);
        return user;
    }

    @Override
    public void toggleActive(@Valid ToggleActiveRequestDTO request) {
        userInputValidator.validate(request, "Toggle active request");
        log.info("Changing active status for user: username={}", request.getUsername());

        String username = request.getUsername();

        transactionManager.performWithinTx(session -> {
            if (isTrainee(username)) {
                Trainee trainee = traineeDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

                boolean currentStatus = trainee.getUser().getIsActive();
                traineeDao.update(trainee.toBuilder().user(trainee.getUser().toBuilder().isActive(!currentStatus).build()).build());

                log.info("Trainee {}: username={}", currentStatus ? "deactivated" : "activated", username);
            } else if (isTrainer(username)) {
                Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

                boolean currentStatus = trainer.getUser().getIsActive();
                trainerDao.update(trainer.toBuilder().user(trainer.getUser().toBuilder().isActive(!currentStatus).build()).build());

                log.info("Trainer {}: username={}", currentStatus ? "deactivated" : "activated", username);
            } else {
                throw new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username));
            }
        });
    }

    private void updatePassword(String username, String encodedPassword) {
        if (isTrainee(username)) {
            updateTraineePassword(username, encodedPassword);
            return;
        }
        updateTrainerPassword(username, encodedPassword);
    }

    private void updateTraineePassword(String username, String encodedPassword) {
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

        traineeDao.update(withNewPassword(trainee, encodedPassword));
    }

    private void updateTrainerPassword(String username, String encodedPassword) {
        Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(format(USER_NOT_FOUND_BY_USERNAME, username)));

        trainerDao.update(withNewPassword(trainer, encodedPassword));
    }

    private Trainee withNewPassword(Trainee trainee, String encodedPassword) {
        return trainee.toBuilder().user(buildUserWithNewPassword(trainee.getUser(), encodedPassword)).build();
    }

    private Trainer withNewPassword(Trainer trainer, String encodedPassword) {
        return trainer.toBuilder().user(buildUserWithNewPassword(trainer.getUser(), encodedPassword)).build();
    }

    private User buildUserWithNewPassword(User user, String encodedPassword) {
        return user.toBuilder().password(encodedPassword).build();
    }

    private boolean isUsernameTaken(String username) {
        return traineeDao.existsByUsername(username) || trainerDao.existsByUsername(username);
    }

    private boolean isTrainee(String username) {
        return traineeDao.existsByUsername(username);
    }

    private boolean isTrainer(String username) {
        return trainerDao.existsByUsername(username);
    }
}
