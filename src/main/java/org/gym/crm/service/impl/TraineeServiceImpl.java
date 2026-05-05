package org.gym.crm.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.gym.crm.util.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {
    private static final String TRAINEE_NOT_FOUND_BY_ID = "Trainee not found by id: %s";
    private static final String TRAINEE = "Trainee";

    private final TraineeDao dao;
    private final UserProfileService userCredentialGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TraineeTrainingCriteriaBuilder criteriaBuilder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainee create(Trainee trainee) {
        Validator.validateNotNull(trainee, TRAINEE);

        log.info("Creating trainee: firstName={} lastName{}", trainee.getUser().getFirstName(), trainee.getUser().getLastName());

        String username = userCredentialGenerator.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName());
        String rawPassword = userCredentialGenerator.generatePassword();

        Trainee withCredentials = trainee.toBuilder()
                .user(
                        trainee.getUser().toBuilder()
                                .username(username)
                                .password(passwordEncoder.encode(rawPassword))
                                .isActive(true)
                                .build()
                )
                .build();

        Trainee saved = dao.save(withCredentials);
        log.info("Trainee created successfully: username={}", saved.getUser().getUsername());

        return saved;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.of(dao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINEE_NOT_FOUND_BY_ID, id))));
    }

    @Override
    public List<Trainee> findAll() {
        log.info("Fetching all trainees");

        List<Trainee> trainees = dao.findAll();

        log.info("Fetched {} trainees", trainees.size());
        return trainees;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Validator.validateNotNull(trainee, TRAINEE);

        log.info("Updating trainee: id={}", trainee.getId());
        findById(trainee.getId());

        Trainee updated = dao.update(trainee);
        log.info("Trainee updated successfully: id={}", updated.getId());

        return updated;
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting trainee: id={}", id);
        findById(id);

        dao.delete(id);
        log.info("Trainee deleted successfully: id={}", id);
    }

    @Override
    public boolean authenticate(String username, String password) {
        Validator.validateNotBlank(username, "Username");
        Validator.validateNotBlank(password, "Password");

        return findByUsername(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        Validator.validateNotBlank(username, "Username");

        return dao.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        Validator.validateNotBlank(username, "Username");
        Validator.validateNotBlank(oldPassword, "Old password");
        Validator.validateNotBlank(newPassword, "New password");

        Trainee trainee = dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        if (!trainee.getUser().getPassword().equals(oldPassword)) {
            throw new AuthenticationException("Current password is incorrect");
        }

        trainee.getUser().setPassword(newPassword);
        dao.save(trainee);
        log.info("Password changed for trainee: {}", username);
    }

    @Override
    public void setActive(String username, boolean active) {
        Validator.validateNotBlank(username, "Username");

        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        boolean currentStatus = trainee.getUser().getIsActive();
        if (currentStatus == active) {
            throw new IllegalStateException(
                    String.format("Trainee '%s' is already %s. Not idempotent.", username, active ? "active" : "inactive")
            );
        }

        trainee.getUser().setIsActive(active);
        dao.save(trainee);
        log.info("Trainee {} set active={}", username, active);
    }

    @Override
    public void deleteByUsername(String username) {
        Validator.validateNotBlank(username, "Username");

        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        dao.delete(trainee);
        log.info("Hard deleted trainee and cascaded trainings for username={}", username);
    }

    @Override
    public List<Training> getTrainings(TraineeTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        var cq = criteriaBuilder.build(
                entityManager.getCriteriaBuilder(), filter);

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        Validator.validateNotBlank(traineeUsername, "Trainee username");

        if (!dao.existsByUsername(traineeUsername)) {
            throw new EntityNotFoundException("Trainee not found: " + traineeUsername);
        }

        return dao.findUnassignedTrainers(traineeUsername);
    }

    @Override
    public Trainee updateTrainers(String traineeUsername, List<String> trainerUsernames) {
        Validator.validateNotBlank(traineeUsername, "Trainee username");
        Validator.validateNotNull(trainerUsernames, "Trainer usernames");

        Trainee trainee = findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeUsername));

        List<Trainer> trainers = dao.findAllByUsernames(trainerUsernames);
        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);

        Trainee saved = dao.save(trainee);
        log.info("Updated trainers list for trainee={}, trainers={}", traineeUsername, trainerUsernames);
        return saved;
    }

    @Override
    public Trainee updateProfile(String username, Trainee updatedData) {
        Validator.validateNotBlank(username, "Username");
        Validator.validateNotNull(updatedData, "Updated data");

        Trainee trainee = dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        User user = trainee.getUser();
        User updatedUser = updatedData.getUser();

        if (updatedUser != null) {
            Validator.validateNotBlank(updatedUser.getFirstName(), "First name");
            Validator.validateNotBlank(updatedUser.getLastName(), "Last name");
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            if (updatedUser.getIsActive() != user.getIsActive()) {
                user.setIsActive(updatedUser.getIsActive());
            }
        }

        if (updatedData.getDateOfBirth() != null) {
            trainee.setDateOfBirth(updatedData.getDateOfBirth());
        }
        if (updatedData.getAddress() != null) {
            trainee.setAddress(updatedData.getAddress());
        }

        Trainee saved = dao.save(trainee);
        log.info("Updated trainee profile: {}", username);
        return saved;
    }
}
