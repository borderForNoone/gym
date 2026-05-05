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
        Validator.validateNotBlank(username, "Username");
        Validator.validateNotBlank(password, "Password");

        return trainerDao.findByUsername(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Validator.validateNotBlank(username, "Username");

        return trainerDao.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        Validator.validateNotBlank(username, "Username");
        Validator.validateNotBlank(oldPassword, "Old password");
        Validator.validateNotBlank(newPassword, "New password");

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        if (!trainer.getUser().getPassword().equals(oldPassword)) {
            throw new AuthenticationException("Current password is incorrect");
        }

        trainer.getUser().setPassword(newPassword);
        trainerDao.save(trainer);
        log.info("Password changed for trainer: {}", username);
    }

    @Override
    public Trainer updateProfile(String username, Trainer updatedData) {
        Validator.validateNotBlank(username, "Username");
        Validator.validateNotNull(updatedData, "Updated data");

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        User updatedUser = updatedData.getUser();

        Trainer.TrainerBuilder<?, ?> trainerBuilder = trainer.toBuilder();

        if (updatedUser != null) {
            Validator.validateNotBlank(updatedUser.getFirstName(), "First name");
            Validator.validateNotBlank(updatedUser.getLastName(), "Last name");

            String newUsername = userProfileService.generateUsername(
                    updatedUser.getFirstName(), updatedUser.getLastName());

            User rebuiltUser = trainer.getUser().toBuilder()
                    .firstName(updatedUser.getFirstName())
                    .lastName(updatedUser.getLastName())
                    .username(newUsername)
                    .build();

            trainerBuilder.user(rebuiltUser);
        }

        if (updatedData.getSpecialization() != null) {
            trainerBuilder.specialization(updatedData.getSpecialization());
        }

        Trainer updatedTrainer = trainerBuilder.build();
        Trainer saved = trainerDao.update(updatedTrainer);

        log.info("Updated trainer profile: {} -> {}", username, saved.getUser().getUsername());
        return saved;
    }

    @Override
    public void setActive(String username, boolean active) {
        Validator.validateNotBlank(username, "Username");

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        boolean currentStatus = trainer.getUser().getIsActive();
        if (currentStatus == active) {
            throw new IllegalStateException(
                    "Trainer '" + username + "' is already " + (active ? "active" : "inactive") + ". Not idempotent.");
        }

        trainer.getUser().setIsActive(active);
        trainerDao.save(trainer);
        log.info("Trainer {} set active={}", username, active);
    }

    @Override
    public List<Training> getTrainings(TrainerTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        var cq = criteriaBuilder.build(
                entityManager.getCriteriaBuilder(), filter);

        return entityManager.createQuery(cq).getResultList();
    }
}
