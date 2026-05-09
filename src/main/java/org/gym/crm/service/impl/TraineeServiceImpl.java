package org.gym.crm.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.User;
import org.gym.crm.search.criteria.TraineeTrainingCriteriaBuilder;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.util.CoreValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {
    private static final String TRAINEE_NOT_FOUND_BY_USERNAME = "Trainee not found: %s";
    private static final String TRAINEE = "Trainee";
    private static final String USERNAME_LABEL = "Username";
    private static final String PASSWORD_LABEL = "Password";
    private static final String OLD_PASSWORD_LABEL = "Old password";
    private static final String NEW_PASSWORD_LABEL = "New password";

    private final TraineeDao dao;
    private final UserProfileService userCredentialGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TraineeTrainingCriteriaBuilder criteriaBuilder;
    private final CoreValidator validator;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public Trainee create(Trainee trainee) {
        validator.validateTrainee(trainee);

        log.info("Creating trainee: firstName={} lastName={}", trainee.getUser().getFirstName(), trainee.getUser().getLastName());

        String username = userCredentialGenerator.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName());
        String rawPassword = userCredentialGenerator.generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User newUser = trainee.getUser().toBuilder()
                .username(username)
                .password(encodedPassword)
                .isActive(true)
                .build();
        Trainee withCredentials = trainee.toBuilder()
                .user(newUser)
                .build();

        Trainee saved = dao.save(withCredentials);
        log.info("Trainee created successfully: username={}", saved.getUser().getUsername());

        return saved;
    }

    @Transactional
    @Override
    public Trainee update(Trainee trainee) {
        validator.validateNotNull(trainee, TRAINEE);

        log.info("Updating trainee: id={}", trainee.getId());
        findByUsername(trainee.getUser().getUsername());

        Trainee updated = dao.update(trainee);
        log.info("Trainee updated successfully: id={}", updated.getId());

        return updated;
    }

    @Override
    public boolean authenticate(String username, String password) {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateNotBlank(password, PASSWORD_LABEL);

        return findByUsername(username)
                .map(t -> passwordEncoder.matches(password, t.getUser().getPassword()))
                .orElse(false);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        validator.validateNotBlank(username, USERNAME_LABEL);

        return dao.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword)
            throws AuthenticationException {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateNotBlank(oldPassword, OLD_PASSWORD_LABEL);
        validator.validateNotBlank(newPassword, NEW_PASSWORD_LABEL);

        Trainee trainee = dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));
        User currentUser = trainee.getUser();

        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        User updatedUser = currentUser.toBuilder()
                .password(encodedPassword)
                .build();
        Trainee updatedTrainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        dao.save(updatedTrainee);
    }

    @Override
    public Trainee setActive(String username, boolean active) {
        validator.validateNotBlank(username, USERNAME_LABEL);

        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));
        User currentUser = trainee.getUser();

        boolean currentState = Boolean.TRUE.equals(currentUser.getIsActive());

        if (currentState == active) {
            throw new IllegalStateException(
                    String.format("Trainee '%s' is already %s. Not idempotent.",
                            username, active ? "active" : "inactive"));
        }

        User updatedUser = currentUser.toBuilder()
                .isActive(active)
                .build();
        Trainee updatedTrainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        dao.save(updatedTrainee);
        return updatedTrainee;
    }

    @Transactional
    @Override
    public void deleteByUsername(String username) {
        validator.validateNotBlank(username, USERNAME_LABEL);

        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));

        dao.delete(trainee);
    }

    @Override
    public List<Training> getTrainings(TraineeTrainingFilter filter) {
        validator.validateNotNull(filter, "Filter");

        CriteriaQuery<Training> searchQuery =
                criteriaBuilder.build(entityManager.getCriteriaBuilder(), filter);

        return entityManager.createQuery(searchQuery).getResultList();
    }

    @Override
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        validator.validateNotBlank(traineeUsername, "Trainee username");

        if (!dao.existsByUsername(traineeUsername)) {
            throw new EntityNotFoundException(
                    String.format(TRAINEE_NOT_FOUND_BY_USERNAME, traineeUsername));
        }

        return dao.findUnassignedTrainers(traineeUsername);
    }

    @Transactional
    @Override
    public Trainee updateTrainers(String traineeUsername, List<String> trainerUsernames) {
        validator.validateNotBlank(traineeUsername, "Trainee username");
        validator.validateNotNull(trainerUsernames, "Trainer usernames");

        Trainee trainee = findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(TRAINEE_NOT_FOUND_BY_USERNAME, traineeUsername)));

        List<Trainer> trainers = dao.findAllByUsernames(trainerUsernames);
        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);

        Trainee saved = dao.save(trainee);
        log.info("Updated trainers list for trainee={}, trainers={}", traineeUsername, trainerUsernames);

        return saved;
    }

    @Override
    public Trainee updateProfile(String username, Trainee updatedData) {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateTrainee(updatedData);

        Trainee trainee = dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));

        User currentUser = trainee.getUser();
        User incomingUser = updatedData.getUser();
        User resultUser = currentUser;

        if (incomingUser != null) {
            resultUser = currentUser.toBuilder()
                    .firstName(incomingUser.getFirstName())
                    .lastName(incomingUser.getLastName())
                    .isActive(incomingUser.getIsActive())
                    .build();
        }

        Trainee.TraineeBuilder<?, ?> builder = trainee.toBuilder()
                .user(resultUser);

        if (updatedData.getDateOfBirth() != null) {
            builder.dateOfBirth(updatedData.getDateOfBirth());
        }
        if (updatedData.getAddress() != null) {
            builder.address(updatedData.getAddress());
        }

        return dao.save(builder.build());
    }
}