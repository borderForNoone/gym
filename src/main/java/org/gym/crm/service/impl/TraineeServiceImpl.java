package org.gym.crm.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.dto.TraineeInfoDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.dto.TraineeUpdateDTO;
import org.gym.crm.dto.TrainerAssignmentUpdateDTO;
import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.mapper.TraineeMapper;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.User;
import org.gym.crm.search.criteria.TraineeTrainingCriteriaBuilder;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.service.common.UserInputValidator;
import org.gym.crm.util.CoreValidator;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

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
    private static final String TRAINER_NOT_FOUND_BY_USERNAME = "Trainer not found by username: %s";

    private final TraineeDao dao;
    private final UserProfileService userCredentialGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TraineeTrainingCriteriaBuilder criteriaBuilder;
    private final CoreValidator validator;
    private final UserInputValidator userInputValidator;
    private final TraineeMapper mapper;
    private final TrainerMapper trainerMapper;
    @Lazy
    private final TrainerService trainerService;

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
    public TraineeResponseDTO update(@Valid TraineeUpdateDTO request) {
        userInputValidator.validate(request, TRAINEE);

        log.info("Updating trainee: username={}", request.getUsername());

        Trainee existing = dao.findByUsername(request.getUsername()).orElseThrow(
                () -> new EntityNotFoundException(String.format(TRAINEE_NOT_FOUND_BY_USERNAME, request.getUsername())));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(request.getIsActive())
                .build();
        Trainee updated = existing.toBuilder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        Trainee saved = dao.update(updated);
        log.info("Trainee updated successfully: username={}", saved.getUser().getUsername());

        return mapper.toDto(saved);
    }

    @Override
    public TraineeInfoDTO getTraineeByUsername(String username) {
        log.info("Getting trainee by username: username={}", username);
        userInputValidator.validateUsername(username);

        Trainee trainee = dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));

        return mapper.toInfoDto(trainee);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword)
            throws AuthenticationException {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateNotBlank(oldPassword, OLD_PASSWORD_LABEL);
        validator.validateNotBlank(newPassword, NEW_PASSWORD_LABEL);

        Trainee trainee = dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));
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

        Trainee trainee = findTraineeOrThrow(username);
        User currentUser = trainee.getUser();

        boolean isActive = currentUser.getIsActive() != null && currentUser.getIsActive();
        if (isActive == active) {
            throw new IllegalStateException(format("Trainer '%s' is already %s. No action taken.",
                    username, active ? "active" : "inactive"));
        }

        User updatedUser = currentUser.toBuilder().isActive(active).build();
        Trainee updatedTrainee = trainee.toBuilder().user(updatedUser).build();

        dao.save(updatedTrainee);
        return updatedTrainee;
    }

    @Transactional
    @Override
    public void deleteByUsername(String username) {
        validator.validateNotBlank(username, USERNAME_LABEL);

        dao.delete(findTraineeOrThrow(username));
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
            throw new EntityNotFoundException(format(TRAINEE_NOT_FOUND_BY_USERNAME, traineeUsername));
        }

        return dao.findUnassignedTrainers(traineeUsername);
    }

    @Transactional
    @Override
    public List<TrainerInfoDTO> updateTrainersList(TrainerAssignmentUpdateDTO dto) {
        userInputValidator.validate(dto, "Trainer assignment");

        log.info("Updating trainers list for trainee: username={}, trainers' usernames={}", dto.getTraineeUsername(), dto.getTrainerUsernames());
        List<Trainer> trainers = dto.getTrainerUsernames().stream()
                .map(username -> trainerService.findByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException(
                                String.format(TRAINER_NOT_FOUND_BY_USERNAME, username))))
                .toList();

        dao.updateTrainersList(dto.getTraineeUsername(), trainers);
        log.info("Trainers list updated successfully: username={}", dto.getTraineeUsername());

        Trainee updatedTrainee = dao.findByUsername(dto.getTraineeUsername())
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINEE_NOT_FOUND_BY_USERNAME, dto.getTraineeUsername())));

        return updatedTrainee.getTrainers().stream()
                .map(trainerMapper::toInfoDto)
                .toList();
    }

    @Override
    public Trainee updateProfile(String username, Trainee updatedData) {
        validator.validateNotBlank(username, USERNAME_LABEL);
        validator.validateTrainee(updatedData);

        Trainee trainee = dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));

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

    private Trainee findTraineeOrThrow(String username) {
        return dao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));
    }
}