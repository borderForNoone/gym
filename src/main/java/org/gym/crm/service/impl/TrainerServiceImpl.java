package org.gym.crm.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.User;
import org.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private static final String USERNAME_LABEL = "Username";
    private static final String PASSWORD_LABEL = "Password";
    private static final String OLD_PASSWORD_LABEL = "Old password";
    private static final String NEW_PASSWORD_LABEL = "New password";
    private static final String UPDATED_DATA_LABEL = "Updated data";
    private static final String FILTER_LABEL = "Filter";
    private static final String TRAINER_NOT_FOUND_PREFIX = "Trainer not found: ";

    private final TrainerDao trainerDao;
    private final UserProfileService userProfileService;
    private final TrainerTrainingCriteriaBuilder criteriaBuilder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainer create(Trainer trainer) {
        log.info("Creating trainer: {} {}", trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        String username = userProfileService.generateUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        String password = userProfileService.generatePassword();

        User user = trainer.getUser().toBuilder()
                .username(username)
                .password(password)
                .build();

        Trainer trainerWithProfile = trainer.toBuilder()
                .user(user)
                .build();

        Trainer saved = trainerDao.save(trainerWithProfile);

        log.info("Trainer created successfully with username={}", username);
        return saved;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Searching trainer by id={}", id);
        return trainerDao.findById(id);
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Fetching all trainers");
        return trainerDao.findAll();
    }

    @Override
    public Trainer update(Trainer trainer) {
        log.info("Updating trainer with id={}", trainer.getId());
        return trainerDao.update(trainer);
    }

    @Override
    public boolean authenticate(String username, String password) {
        Validator.validateNotBlank(username, USERNAME_LABEL);
        Validator.validateNotBlank(password, PASSWORD_LABEL);

        return trainerDao.findByUsername(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        return trainerDao.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        Validator.validateNotBlank(username, USERNAME_LABEL);
        Validator.validateNotBlank(oldPassword, OLD_PASSWORD_LABEL);
        Validator.validateNotBlank(newPassword, NEW_PASSWORD_LABEL);

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINER_NOT_FOUND_PREFIX + username));

        if (!trainer.getUser().getPassword().equals(oldPassword)) {
            throw new AuthenticationException("Current password is incorrect");
        }

        trainer.getUser().setPassword(newPassword);
        trainerDao.save(trainer);
    }

    @Override
    public Trainer updateProfile(String username, Trainer updatedData) {
        Validator.validateNotBlank(username, USERNAME_LABEL);
        Validator.validateNotNull(updatedData, UPDATED_DATA_LABEL);

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINER_NOT_FOUND_PREFIX + username));

        User updatedUser = updatedData.getUser();

        Trainer.TrainerBuilder<?, ?> builder = trainer.toBuilder();

        if (updatedUser != null) {
            Validator.validateNotBlank(updatedUser.getFirstName(), "First name");
            Validator.validateNotBlank(updatedUser.getLastName(), "Last name");

            String newUsername = userProfileService.generateUsername(
                    updatedUser.getFirstName(),
                    updatedUser.getLastName()
            );

            User rebuiltUser = trainer.getUser().toBuilder()
                    .firstName(updatedUser.getFirstName())
                    .lastName(updatedUser.getLastName())
                    .username(newUsername)
                    .build();

            builder.user(rebuiltUser);
        }

        if (updatedData.getSpecialization() != null) {
            builder.specialization(updatedData.getSpecialization());
        }

        return trainerDao.update(builder.build());
    }

    @Override
    public void setActive(String username, boolean active) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINER_NOT_FOUND_PREFIX + username));

        if (trainer.getUser().getIsActive() == active) {
            throw new IllegalStateException(
                    "Trainer '" + username + "' is already " +
                            (active ? "active" : "inactive") +
                            ". Not idempotent."
            );
        }

        trainer.getUser().setIsActive(active);
        trainerDao.save(trainer);
    }

    @Override
    public List<Training> getTrainings(TrainerTrainingFilter filter) {
        Validator.validateNotNull(filter, FILTER_LABEL);

        var cq = criteriaBuilder.build(
                entityManager.getCriteriaBuilder(), filter
        );

        return entityManager.createQuery(cq).getResultList();
    }
}
