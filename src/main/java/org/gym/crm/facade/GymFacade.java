package org.gym.crm.facade;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.dto.TraineeUpdateDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainerUpdateDTO;
import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.mapper.TraineeMapper;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.mapper.TrainingMapper;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GymFacade {
    private static final String TRAINEE_NOT_FOUND = "Trainee not found";
    private static final String TRAINER_NOT_FOUND = "Trainer not found";

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Setter(onMethod_ = {@Autowired})
    private TraineeMapper traineeMapper;
    @Setter(onMethod_ = {@Autowired})
    private TrainerMapper trainerMapper;
    @Setter(onMethod_ = {@Autowired})
    private TrainingMapper trainingMapper;

    public TraineeResponseDTO createTrainee(TraineeRequestDTO traineeRequestDTO) {
        Trainee trainee = traineeMapper.toEntity(traineeRequestDTO);
        Trainee saved = traineeService.create(trainee);

        return traineeMapper.toDto(saved);
    }

    public TraineeResponseDTO updateTrainee(String username, String password, TraineeUpdateDTO traineeUpdateDTO) {
        traineeService.authenticate(username, password);

        Trainee updatedData = traineeMapper.toEntity(traineeUpdateDTO);
        Trainee saved = traineeService.updateProfile(username, updatedData);

        return traineeMapper.toDto(saved);
    }

    public void setTraineeActive(String username, String password, boolean active) {
        traineeService.authenticate(username, password);

        traineeService.setActive(username, active);
    }

    public void setTrainerActive(String username, String password, boolean active) {
        trainerService.authenticate(username, password);

        trainerService.setActive(username, active);
    }

    public void deleteTraineeByUsername(String username, String password) {
        traineeService.authenticate(username, password);

        traineeService.deleteByUsername(username);
    }

    public List<TrainingResponseDTO> getTraineeTrainings(String username, String password, TraineeTrainingFilter filter) {
        traineeService.authenticate(username, password);

        return traineeService.getTrainings(filter)
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    public List<TrainingResponseDTO> getTrainerTrainings(String username, String password, TrainerTrainingFilter filter) {
        trainerService.authenticate(username, password);

        return trainerService.getTrainings(filter)
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    public TrainingResponseDTO createTraining(String username, String password, TrainingRequestDTO trainingRequestDTO) {
        traineeService.authenticate(username, password);

        Training training = trainingMapper.toEntity(trainingRequestDTO);
        Training saved = trainingService.create(training);

        return trainingMapper.toDto(saved);
    }

    public List<TrainerResponseDTO> getUnassignedTrainers(String username, String password) {
        traineeService.authenticate(username, password);

        return traineeService.getUnassignedTrainers(username)
                .stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    public TraineeResponseDTO updateTraineeTrainers(String username, String password, List<String> trainerUsernames) {
        traineeService.authenticate(username, password);

        Trainee updated = traineeService.updateTrainers(username, trainerUsernames);

        return traineeMapper.toDto(updated);
    }

    public TraineeResponseDTO updateTrainee(TraineeUpdateDTO traineeUpdateDTO) {
        Trainee trainee = traineeMapper.toEntity(traineeUpdateDTO);
        Trainee saved = traineeService.update(trainee);

        return traineeMapper.toDto(saved);
    }

    public TrainerResponseDTO createTrainer(TrainerRequestDTO trainerRequestDTO) {
        Trainer trainer = trainerMapper.toEntity(trainerRequestDTO);
        Trainer saved = trainerService.create(trainer);

        return trainerMapper.toDto(saved);
    }

    public TrainerResponseDTO getTrainerByUsername(String username, String password) {
        trainerService.authenticate(username, password);

        Trainer trainer = trainerService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(TRAINER_NOT_FOUND));

        return trainerMapper.toDto(trainer);
    }

    public TraineeResponseDTO getTraineeByUsername(String username, String password) {
        traineeService.authenticate(username, password);

        Trainee trainee = traineeService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(TRAINEE_NOT_FOUND));

        return traineeMapper.toDto(trainee);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword)
            throws AuthenticationException {
        traineeService.authenticate(username, oldPassword);

        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword)
            throws AuthenticationException {
        trainerService.authenticate(username, oldPassword);

        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public TrainerResponseDTO updateTrainer(TrainerUpdateDTO trainerUpdateDTO) {
        trainerService.authenticate(trainerUpdateDTO.getUsername(), trainerUpdateDTO.getPassword());

        Trainer trainer = trainerMapper.toEntity(trainerUpdateDTO);
        Trainer saved = trainerService.update(trainer);

        return trainerMapper.toDto(saved);
    }

    public TrainerResponseDTO updateTrainer(String username, String password, TrainerUpdateDTO trainerUpdateDTO) {
        trainerService.authenticate(username, password);

        Trainer updatedData = trainerMapper.toEntity(trainerUpdateDTO);
        Trainer saved = trainerService.updateProfile(username, updatedData);

        return trainerMapper.toDto(saved);
    }
}
