package org.gym.crm.facade;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.gym.crm.auth.Authenticated;
import org.gym.crm.dto.PasswordChangeRequest;
import org.gym.crm.dto.ToggleActiveRequestDTO;
import org.gym.crm.dto.TraineeInfoDTO;
import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.dto.TraineeUpdateDTO;
import org.gym.crm.dto.TrainerAssignmentUpdateDTO;
import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainerUpdateDTO;
import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.mapper.TraineeMapper;
import org.gym.crm.mapper.TraineeRestMapper;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.mapper.TrainerRestMapper;
import org.gym.crm.mapper.TrainingMapper;
import org.gym.crm.mapper.TrainingRestMapper;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Training;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.GetTrainerTrainingResponse;
import org.gym.crm.rest.LoginChangeRequest;
import org.gym.crm.rest.LoginRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.rest.TrainerUpdateRequest;
import org.gym.crm.rest.TrainerUpdateResponse;
import org.gym.crm.rest.TrainingTypeResponse;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.TrainingService;
import org.gym.crm.service.UserProfileService;
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
    private final UserProfileService userProfileService;
    private final TraineeRestMapper traineeRestMapper;
    private final TrainerRestMapper trainerRestMapper;
    private final TrainingRestMapper trainingRestMapper;

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

    @Authenticated
    public TraineeUpdateResponse updateTrainee(TraineeUpdateRequest request, String username) {
        TraineeUpdateDTO dto = traineeRestMapper.toDto(username, request);
        TraineeResponseDTO traineeResponseDTO = traineeService.update(dto);

        return traineeRestMapper.toRestUpdateResponse(traineeResponseDTO);
    }

    @Authenticated
    public void toggleActiveStatus(ActivationStatusRequest request, String username) {
        ToggleActiveRequestDTO dto = ToggleActiveRequestDTO.builder()
                .username(username)
                .isActive(request.getIsActive())
                .build();

        userProfileService.toggleActive(dto);
    }

    @Authenticated
    public void setTraineeActive(String username, boolean active) {
        traineeService.setActive(username, active);
    }

    @Authenticated
    public void setTrainerActive(String username, boolean active) {
        trainerService.setActive(username, active);
    }

    @Authenticated
    public void deleteTraineeByUsername(String username) {
        traineeService.deleteByUsername(username);
    }

    @Authenticated
    public List<TrainingResponseDTO> getTraineeTrainings(TraineeTrainingFilter filter) {
        return traineeService.getTrainings(filter)
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    @Authenticated
    public List<GetTraineeTrainingResponse> getTraineeTrainingsByFilter(TraineeTrainingFilter filter) {
        return trainingService.getTraineeTrainings(filter).stream()
                .map(trainingRestMapper::toRestTraineeResponse)
                .toList();
    }

    @Authenticated
    public List<GetTrainerTrainingResponse> getTrainerTrainingsByFilter(TrainerTrainingFilter filter) {
        return trainingService.getTrainerTrainings(filter).stream()
                .map(trainingRestMapper::toRestTrainerResponse)
                .toList();
    }

    @Authenticated
    public List<TrainingResponseDTO> getTrainerTrainings(TrainerTrainingFilter filter) {
        return trainerService.getTrainings(filter)
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    @Authenticated
    public TrainingResponseDTO createTraining(TrainingRequestDTO trainingRequestDTO) {
        Training training = trainingMapper.toEntity(trainingRequestDTO);
        Training saved = trainingService.create(training);

        return trainingMapper.toDto(saved);
    }

    @Authenticated
    public List<TrainerResponseDTO> getUnassignedTrainers(String username) {
        return traineeService.getUnassignedTrainers(username)
                .stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    @Authenticated
    public TraineeAssignedTrainersUpdateResponse updateTraineeTrainersList(TraineeAssignedTrainersUpdateRequest request, String username) {
        TrainerAssignmentUpdateDTO dto = TrainerAssignmentUpdateDTO.builder()
                .traineeUsername(username)
                .trainerUsernames(request.getTrainerUsernames())
                .build();
        List<TrainerInfoDTO> list = traineeService.updateTrainersList(dto);
        List<AssignedTrainerResponse> assignedTrainers = list.stream()
                .map(trainerRestMapper::toRest)
                .toList();

        TraineeAssignedTrainersUpdateResponse response = new TraineeAssignedTrainersUpdateResponse();
        response.setTrainers(assignedTrainers);

        return response;
    }

    public TrainerCreateResponse createTrainer(TrainerCreateRequest request) {
        TrainerRequestDTO dto = trainerRestMapper.toDto(request);
        TrainerResponseDTO trainerResponseDTO = trainerService.createTrainer(dto);

        return trainerRestMapper.toRest(trainerResponseDTO);
    }

    @Authenticated
    public TrainerGetResponse getTrainerByUsername(String username) {
        TrainerInfoDTO trainerInfoDTO = trainerService.getTrainerByUsername(username);

        return trainerRestMapper.toRestGetResponse(trainerInfoDTO);
    }

    @Authenticated
    public TraineeGetResponse getTraineeByUsername(String username) {
        TraineeInfoDTO traineeInfoDTO = traineeService.getTraineeByUsername(username);

        return traineeRestMapper.toRest(traineeInfoDTO);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword)
            throws AuthenticationException {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    @Authenticated
    public void changeTrainerPassword(String username, String oldPassword, String newPassword)
            throws AuthenticationException {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    @Authenticated
    public TrainerUpdateResponse updateTrainer(TrainerUpdateRequest request, String username) {
        TrainerUpdateDTO dto = trainerRestMapper.toDto(username, request);
        TrainerResponseDTO trainerResponseDTO = trainerService.updateTrainer(dto);

        return trainerRestMapper.toRestUpdateResponse(trainerResponseDTO);
    }

    @Authenticated
    public void changePassword(LoginChangeRequest request, String username) {
        PasswordChangeRequest requestDTO = PasswordChangeRequest.builder()
                .username(request.getUsername())
                .oldPassword(request.getOldPassword())
                .newPassword(request.getNewPassword())
                .build();

        userProfileService.changePassword(requestDTO);
    }

    public void login(LoginRequest request) {
        userProfileService.login(request);
    }

    @Authenticated
    public List<AssignedTrainerResponse> getTrainersNotAssignedToTrainee(String username) {
        List<TrainerInfoDTO> trainers = trainerService.getNotAssignedToTrainee(username);

        return trainers.stream()
                .map(trainerRestMapper::toRest)
                .toList();
    }

    @Authenticated
    public List<TrainingTypeResponse> getTrainingTypes() {
        return trainingService.getAllTrainingTypes().stream()
                .map(trainingRestMapper::toRest)
                .toList();
    }
}
