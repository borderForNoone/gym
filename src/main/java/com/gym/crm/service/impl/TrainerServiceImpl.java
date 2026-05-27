package com.gym.crm.service.impl;

import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.exception.InvalidPasswordException;
import com.gym.crm.facade.dto.CreatedTrainer;
import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.facade.dto.TrainerRequestDTO;
import com.gym.crm.facade.dto.TrainerResponseDTO;
import com.gym.crm.facade.dto.TrainerUpdateDTO;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.CoreValidator;
import com.gym.crm.service.common.UserInputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.List;

import static com.gym.crm.model.FieldName.TRAINER;
import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private static final String USERNAME_LABEL = "Username";
    private static final String FILTER_LABEL = "Filter";
    private static final String TRAINER_NOT_FOUND = "Trainer not found: %s";
    private static final String TRAINING_TYPE_NOT_FOUND_BY_NAME = "Training type not found by name: %s";
    private static final String TRAINER_NOT_FOUND_BY_USERNAME = "Trainer not found by username: %s";

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserProfileService userProfileService;
    private final TrainerTrainingCriteriaBuilder criteriaBuilder;
    private final CoreValidator validator;
    private final UserInputValidator userInputValidator;
    private final PasswordEncoder passwordEncoder;
    private final TrainerMapper mapper;
    private final TrainingTypeRepository trainingTypeRepository;

    @Transactional
    @Override
    public CreatedTrainer createTrainer(TrainerRequestDTO request) {
        userInputValidator.validate(request, TRAINER.name());

        log.info("Creating trainer: firstName={} lastName={}", request.getFirstName(), request.getLastName());

        Trainer trainer = mapper.toEntity(request);

        String username = userProfileService.generateUsername(request.getFirstName(), request.getLastName());
        String rawPassword = userProfileService.generatePassword();

        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(request.getSpecialization())
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINING_TYPE_NOT_FOUND_BY_NAME, request.getSpecialization())));

        User user = trainer.getUser().toBuilder().username(username).password(passwordEncoder.encode(rawPassword)).isActive(true).build();

        Trainer saved = trainerRepository.save(trainer.toBuilder().user(user).specialization(trainingType).build());

        return new CreatedTrainer(saved, rawPassword);
    }

    @Transactional
    @Override
    public TrainerResponseDTO updateTrainer(@Valid TrainerUpdateDTO request) {
        userInputValidator.validate(request, TRAINER.name());

        Trainer existing = trainerRepository.findByUser_Username(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINER_NOT_FOUND_BY_USERNAME, request.getUsername())));
        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(request.getSpecialization())
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINING_TYPE_NOT_FOUND_BY_NAME, request.getSpecialization())));

        User user = existing.getUser().toBuilder().firstName(request.getFirstName()).lastName(request.getLastName()).isActive(request.getIsActive()).build();
        Trainer updated = existing.toBuilder().user(user).specialization(trainingType).build();

        return mapper.toDto(trainerRepository.save(updated));
    }

    @Transactional(readOnly = true)
    @Override
    public TrainerInfoDTO getTrainerByUsername(String username) {
        userInputValidator.validateUsername(username);

        Trainer trainer = trainerRepository.findByUser_Username(username).orElseThrow(() -> new EntityNotFoundException(String.format(TRAINER_NOT_FOUND_BY_USERNAME, username)));

        return mapper.toInfoDto(trainer);
    }

    @Transactional
    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        Trainer trainer = trainerRepository.findByUser_Username(username).orElseThrow(() -> new EntityNotFoundException(String.format(TRAINER_NOT_FOUND, username)));
        User currentUser = trainer.getUser();

        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        User updatedUser = currentUser.toBuilder().password(passwordEncoder.encode(newPassword)).build();

        trainerRepository.save(trainer.toBuilder().user(updatedUser).build());
    }

    @Transactional
    @Override
    public Trainer updateProfile(String username, Trainer updatedData) {
        Trainer trainer = trainerRepository.findByUser_Username(username).orElseThrow(() -> new EntityNotFoundException(String.format(TRAINER_NOT_FOUND, username)));

        User updatedUser = updatedData.getUser();
        Trainer.TrainerBuilder<?, ?> builder = trainer.toBuilder();

        if (updatedUser != null) {
            User rebuiltUser = trainer.getUser().toBuilder().firstName(updatedUser.getFirstName()).lastName(updatedUser.getLastName()).build();
            builder.user(rebuiltUser);
        }

        if (updatedData.getSpecialization() != null) {
            builder.specialization(updatedData.getSpecialization());
        }

        return trainerRepository.save(builder.build());
    }

    @Transactional
    @Override
    public void setActive(String username, boolean active) {
        CoreValidator.validateNotBlank(username, USERNAME_LABEL);

        Trainer trainer = trainerRepository.findByUser_Username(username).orElseThrow(() -> new EntityNotFoundException(format(TRAINER_NOT_FOUND, username)));
        User currentUser = trainer.getUser();

        boolean isActive = currentUser.getIsActive() != null && currentUser.getIsActive();
        if (isActive == active) {
            throw new IllegalStateException(format("Trainer '%s' is already %s. No action taken.", username, active ? "active" : "inactive"));
        }

        User updatedUser = currentUser.toBuilder().isActive(active).build();
        trainerRepository.save(trainer.toBuilder().user(updatedUser).build());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Training> getTrainings(TrainerTrainingFilter filter) {
        validator.validateNotNull(filter, FILTER_LABEL);

        return trainingRepository.findByTrainerCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate());
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrainerInfoDTO> getNotAssignedToTrainee(String traineeUsername) {
        return trainerRepository.findAllNotAssignedToTrainee(traineeUsername);
    }
}