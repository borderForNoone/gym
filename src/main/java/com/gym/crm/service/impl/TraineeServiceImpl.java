package com.gym.crm.service.impl;

import com.gym.crm.facade.dto.CreatedTrainee;
import com.gym.crm.facade.dto.TraineeInfoDTO;
import com.gym.crm.facade.dto.TraineeResponseDTO;
import com.gym.crm.facade.dto.TraineeUpdateDTO;
import com.gym.crm.facade.dto.TrainerAssignmentUpdateDTO;
import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.CoreValidator;
import com.gym.crm.service.common.UserInputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserProfileService userCredentialGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TraineeMapper mapper;
    private final TrainerMapper trainerMapper;
    private final CoreValidator validator;
    private final UserInputValidator userInputValidator;

    @Transactional
    @Override
    public CreatedTrainee create(Trainee trainee) {
        validator.validateTrainee(trainee);

        String username = userCredentialGenerator.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName());
        String rawPassword = userCredentialGenerator.generatePassword();

        User newUser = trainee.getUser().toBuilder().username(username).password(passwordEncoder.encode(rawPassword)).isActive(true).build();

        Trainee saved = traineeRepository.save(trainee.toBuilder().user(newUser).build());

        return new CreatedTrainee(saved, rawPassword);
    }

    @Transactional
    @Override
    public TraineeResponseDTO update(@Valid TraineeUpdateDTO request) {
        userInputValidator.validate(request, "Trainee");

        Trainee existing = traineeRepository.findByUser_Username(request.getUsername()).orElseThrow();
        User updatedUser = existing.getUser().toBuilder().firstName(request.getFirstName()).lastName(request.getLastName()).isActive(request.getIsActive()).build();
        Trainee updated = existing.toBuilder().user(updatedUser).address(request.getAddress()).dateOfBirth(request.getDateOfBirth()).build();

        return mapper.toDto(traineeRepository.save(updated));
    }

    @Override
    public TraineeInfoDTO getTraineeByUsername(String username) {
        userInputValidator.validateUsername(username);

        Trainee trainee = traineeRepository.findByUser_Username(username).orElseThrow();

        return mapper.toInfoDto(trainee);
    }

    @Transactional
    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Trainee trainee = traineeRepository.findByUser_Username(username).orElseThrow();
        User user = trainee.getUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

        user = user.toBuilder().password(passwordEncoder.encode(newPassword)).build();

        traineeRepository.save(trainee.toBuilder().user(user).build());
    }

    @Transactional
    @Override
    public Trainee setActive(String username, boolean active) {
        Trainee trainee = traineeRepository.findByUser_Username(username).orElseThrow();
        User user = trainee.getUser();

        if (Boolean.TRUE.equals(user.getIsActive()) == active) {
            throw new IllegalStateException("Already in this state");
        }

        user = user.toBuilder().isActive(active).build();

        return traineeRepository.save(trainee.toBuilder().user(user).build());
    }

    @Transactional
    @Override
    public void deleteByUsername(String username) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow();

        traineeRepository.delete(trainee);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Training> getTrainings(TraineeTrainingFilter filter) {
        return trainingRepository.findTraineeTrainings(
                filter.getUsername(),
                filter.getFromDate(),
                filter.getToDate()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<Trainer> getUnassignedTrainers(String username) {

        Trainee trainee = traineeRepository.findByUser_Username(username).orElseThrow();

        return trainerRepository.findAll().stream()
                .filter(t -> !trainee.getTrainers().contains(t))
                .toList();
    }

    @Transactional
    @Override
    public List<TrainerInfoDTO> updateTrainersList(TrainerAssignmentUpdateDTO dto) {
        Trainee trainee = traineeRepository.findByUser_Username(dto.getTraineeUsername()).orElseThrow();
        List<Trainer> trainers = dto.getTrainerUsernames().stream()
                .map(username -> trainerRepository.findByUser_Username(username).orElseThrow())
                .toList();

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);

        return trainerRepository.findAllById(trainee.getTrainers().stream().map(Trainer::getId).toList()).stream()
                .map(trainerMapper::toInfoDto)
                .toList();
    }

    @Transactional()
    @Override
    public Trainee updateProfile(String username, Trainee updatedData) {
        Trainee trainee = traineeRepository.findByUser_Username(username).orElseThrow();

        User currentUser = trainee.getUser();
        User incoming = updatedData.getUser();

        if (incoming != null) {
            currentUser = currentUser.toBuilder().firstName(incoming.getFirstName()).lastName(incoming.getLastName()).isActive(incoming.getIsActive()).build();
        }

        Trainee result = trainee.toBuilder().user(currentUser).dateOfBirth(updatedData.getDateOfBirth()).address(updatedData.getAddress()).build();

        return traineeRepository.save(result);
    }
}