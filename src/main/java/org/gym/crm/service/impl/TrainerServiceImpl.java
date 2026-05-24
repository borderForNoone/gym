package org.gym.crm.service.impl;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.config.TransactionManager;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.dao.TrainingTypeDao;
import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainerUpdateDTO;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.exception.InvalidPasswordException;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.service.common.UserInputValidator;
import org.gym.crm.util.CoreValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;

import static java.lang.String.format;
import static org.gym.crm.model.FieldName.TRAINER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private static final String USERNAME_LABEL = "Username";
    private static final String PASSWORD_LABEL = "Password";
    private static final String OLD_PASSWORD_LABEL = "Old password";
    private static final String NEW_PASSWORD_LABEL = "New password";
    private static final String FILTER_LABEL = "Filter";
    private static final String TRAINER_NOT_FOUND = "Trainer not found: %s";
    private static final String TRAINING_TYPE_NOT_FOUND_BY_NAME = "Training type not found by name: %s";
    private static final String TRAINER_NOT_FOUND_BY_USERNAME = "Trainer not found by username: %s";

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDAO;
    private final UserProfileService userProfileService;
    private final TrainerTrainingCriteriaBuilder criteriaBuilder;
    private final CoreValidator validator;
    private final UserInputValidator userInputValidator;
    private final PasswordEncoder passwordEncoder;
    private final TrainerMapper mapper;
    private final TransactionManager transactionManager;

    @Override
    public TrainerResponseDTO createTrainer(TrainerRequestDTO request) {
        userInputValidator.validate(request, TRAINER.name());

        log.info("Creating trainer: firstName={} lastName={}", request.getFirstName(), request.getLastName());

        return transactionManager.performReturningWithinTx(session -> {
            Trainer trainer = mapper.toEntity(request);
            String username = userProfileService.generateUsername(request.getFirstName(), request.getLastName());
            String rawPassword = userProfileService.generatePassword();

            TrainingType trainingType = trainingTypeDAO.findByTrainingTypeName(request.getSpecialization())
                    .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINING_TYPE_NOT_FOUND_BY_NAME, request.getSpecialization())));

            User user = trainer.getUser().toBuilder().username(username).password(passwordEncoder.encode(rawPassword)).isActive(true).build();
            Trainer withCredentials = trainer.toBuilder().user(user).specialization(trainingType).build();

            Trainer saved = trainerDao.save(withCredentials);
            log.info("Trainer created successfully: username={}", saved.getUser().getUsername());

            return mapper.toDto(saved);
        });
    }

    @Override
    public TrainerResponseDTO updateTrainer(@Valid TrainerUpdateDTO request) {
        userInputValidator.validate(request, TRAINER.name());

        return transactionManager.performReturningWithinTx(session -> {
            log.info("Updating trainer: username={}", request.getUsername());

            Trainer existing = trainerDao.findByUsername(request.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINER_NOT_FOUND_BY_USERNAME, request.getUsername())));
            TrainingType trainingType = trainingTypeDAO.findByTrainingTypeName(request.getSpecialization())
                    .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINING_TYPE_NOT_FOUND_BY_NAME, request.getSpecialization())));

            User user = existing.getUser().toBuilder().firstName(request.getFirstName()).lastName(request.getLastName()).isActive(request.getIsActive()).build();
            Trainer updated = existing.toBuilder().user(user).specialization(trainingType).build();

            Trainer saved = trainerDao.update(updated);
            log.info("Trainer updated successfully: username={}", saved.getUser().getUsername());

            return mapper.toDto(saved);
        });
    }

    @Override
    public TrainerInfoDTO getTrainerByUsername(String username) {
        log.info("Getting trainer by username: username={}", username);
        userInputValidator.validateUsername(username);

        Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(String.format(TRAINER_NOT_FOUND_BY_USERNAME, username)));

        return mapper.toInfoDto(trainer);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateNotBlank(oldPassword, OLD_PASSWORD_LABEL);
        validator.validateNotBlank(newPassword, NEW_PASSWORD_LABEL);

        transactionManager.performWithinTx(session -> {
            Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(format(TRAINER_NOT_FOUND, username)));
            User currentUser = trainer.getUser();

            if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
                throw new InvalidPasswordException("Current password is incorrect");
            }

            User updatedUser = currentUser.toBuilder().password(passwordEncoder.encode(newPassword)).build();
            trainerDao.save(trainer.toBuilder().user(updatedUser).build());
        });
    }

    @Override
    public Trainer updateProfile(String username, Trainer updatedData) {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateTrainer(updatedData);

        return transactionManager.performReturningWithinTx(session -> {
            Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(format(TRAINER_NOT_FOUND, username)));

            User updatedUser = updatedData.getUser();
            Trainer.TrainerBuilder<?, ?> builder = trainer.toBuilder();

            if (updatedUser != null) {
                User rebuiltUser = trainer.getUser().toBuilder().firstName(updatedUser.getFirstName()).lastName(updatedUser.getLastName()).build();
                builder.user(rebuiltUser);
            }

            if (updatedData.getSpecialization() != null) {
                builder.specialization(updatedData.getSpecialization());
            }

            return trainerDao.update(builder.build());
        });
    }

    @Override
    public void setActive(String username, boolean active) {
        validator.validateNotBlank(username, USERNAME_LABEL);

        transactionManager.performWithinTx(session -> {
            Trainer trainer = trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(format(TRAINER_NOT_FOUND, username)));
            User currentUser = trainer.getUser();

            boolean isActive = currentUser.getIsActive() != null && currentUser.getIsActive();
            if (isActive == active) {
                throw new IllegalStateException(format("Trainer '%s' is already %s. No action taken.", username, active ? "active" : "inactive"));
            }

            User updatedUser = currentUser.toBuilder().isActive(active).build();
            trainerDao.save(trainer.toBuilder().user(updatedUser).build());
        });
    }

    @Override
    public List<Training> getTrainings(TrainerTrainingFilter filter) {
        validator.validateNotNull(filter, FILTER_LABEL);

        return transactionManager.performReturningWithinTx(session -> {
            CriteriaQuery<Training> searchQuery = criteriaBuilder.build(session.getCriteriaBuilder(), filter);

            return session.createQuery(searchQuery).getResultList();
        });
    }

    @Override
    public List<TrainerInfoDTO> getNotAssignedToTrainee(String traineeUsername) {
        userInputValidator.validateUsername(traineeUsername);

        log.info("Getting all trainers not assigned to trainee: username={}", traineeUsername);

        return trainerDao.findNotAssignedToTrainee(traineeUsername).stream()
                .map(mapper::toInfoDto)
                .toList();
    }
}