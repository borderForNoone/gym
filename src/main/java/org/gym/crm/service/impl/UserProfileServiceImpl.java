package org.gym.crm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.util.CoreValidator;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final CoreValidator validator;

    @Override
    public String generateUsername(String firstName, String lastName) {
        validator.validateNotBlank(firstName, "First name");
        validator.validateNotBlank(lastName, "Last name");

        String baseUsername = firstName.trim() + "." + lastName.trim();
        validator.validateUsernameLength(baseUsername);

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

    private boolean isUsernameTaken(String username) {
        return traineeDao.existsByUsername(username) || trainerDao.existsByUsername(username);
    }
}
