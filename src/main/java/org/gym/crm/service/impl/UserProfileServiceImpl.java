package org.gym.crm.service.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
public class UserProfileServiceImpl implements UserProfileService {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    @Setter
    private TraineeDao traineeDao;

    @Autowired
    @Setter
    private TrainerDao trainerDao;

    @Override
    public String generateUsername(String firstName, String lastName) {
        Validator.validateNotBlank(firstName, "First name");
        Validator.validateNotBlank(lastName, "Last name");

        String baseUsername = firstName + "." + lastName;
        Validator.validateUsernameLength(baseUsername);

        if (!usernameExists(baseUsername)) {
            log.debug("Generated username='{}'", baseUsername);
            return baseUsername;
        }

        long suffix = 1;
        String candidate;
        do {
            candidate = baseUsername + suffix;
            suffix++;
        } while (usernameExists(candidate));

        log.debug("Generated username='{}' with suffix due to duplicates", candidate);
        return candidate;
    }

    @Override
    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        log.debug("Generated password of length={}", PASSWORD_LENGTH);
        return password.toString();
    }

    private boolean usernameExists(String username) {
        return traineeDao.existsByUsername(username) ||
                trainerDao.existsByUsername(username);
    }
}
