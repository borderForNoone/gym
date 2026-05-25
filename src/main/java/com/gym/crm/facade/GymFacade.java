package com.gym.crm.facade;

import com.gym.crm.auth.Authenticated;
import com.gym.crm.auth.SessionContext;
import com.gym.crm.dto.CreatedTrainee;
import com.gym.crm.dto.CreatedTrainer;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.ToggleActiveRequestDTO;
import com.gym.crm.dto.TraineeInfoDTO;
import com.gym.crm.dto.TraineeRequestDTO;
import com.gym.crm.dto.TraineeResponseDTO;
import com.gym.crm.dto.TraineeUpdateDTO;
import com.gym.crm.dto.TrainerAssignmentUpdateDTO;
import com.gym.crm.dto.TrainerInfoDTO;
import com.gym.crm.dto.TrainerRequestDTO;
import com.gym.crm.dto.TrainerResponseDTO;
import com.gym.crm.dto.TrainerUpdateDTO;
import com.gym.crm.dto.TrainingRequestDTO;
import com.gym.crm.dto.TrainingResponseDTO;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.mapper.TraineeRestMapper;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.mapper.TrainerRestMapper;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.mapper.TrainingRestMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Training;
import com.gym.crm.model.User;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.GetTrainerTrainingResponse;
import org.gym.crm.rest.LoginChangeRequest;
import org.gym.crm.rest.LoginRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeCreateRequest;
import org.gym.crm.rest.TraineeCreateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.rest.TrainerUpdateRequest;
import org.gym.crm.rest.TrainerUpdateResponse;
import org.gym.crm.rest.TrainingCreateRequest;
import org.gym.crm.rest.TrainingTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final UserProfileService userProfileService;
    private final TraineeRestMapper traineeRestMapper;
    private final TrainerRestMapper trainerRestMapper;
    private final TrainingRestMapper trainingRestMapper;
    private final SessionContext sessionContext;

    @Setter(onMethod_ = {@Autowired})
    private TraineeMapper traineeMapper;
    @Setter(onMethod_ = {@Autowired})
    private TrainerMapper trainerMapper;
    @Setter(onMethod_ = {@Autowired})
    private TrainingMapper trainingMapper;

    public TraineeCreateResponse createTrainee(TraineeCreateRequest request) {
        TraineeRequestDTO dto = traineeRestMapper.toDto(request);
        Trainee trainee = traineeMapper.toEntity(dto);
        CreatedTrainee created = traineeService.create(trainee);
        TraineeResponseDTO responseDTO = traineeMapper.toDto(created.trainee()).toBuilder().password(created.rawPassword()).build();

        return traineeRestMapper.toRest(responseDTO);
    }

    @Authenticated
    public TraineeUpdateResponse updateTrainee(TraineeUpdateRequest request, String username) {
        TraineeUpdateDTO dto = traineeRestMapper.toDto(username, request);
        TraineeResponseDTO traineeResponseDTO = traineeService.update(dto);

        return traineeRestMapper.toRestUpdateResponse(traineeResponseDTO);
    }

    @Authenticated
    public void toggleActiveStatus(ActivationStatusRequest request, String username) {
        ToggleActiveRequestDTO dto = ToggleActiveRequestDTO.builder().username(username).isActive(request.getIsActive()).build();

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
        return traineeService.getTrainings(filter).stream()
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
        return trainerService.getTrainings(filter).stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    @Authenticated
    public TrainingResponseDTO createTraining(TrainingCreateRequest request) {
        TrainingRequestDTO dto = trainingRestMapper.toDto(request);
        Training training = trainingMapper.toEntity(dto);
        Training saved = trainingService.create(training);

        return trainingMapper.toDto(saved);
    }

    @Authenticated
    public List<TrainerResponseDTO> getUnassignedTrainers(String username) {
        return traineeService.getUnassignedTrainers(username).stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    @Authenticated
    public TraineeAssignedTrainersUpdateResponse updateTraineeTrainersList(TraineeAssignedTrainersUpdateRequest request, String username) {
        TrainerAssignmentUpdateDTO dto = TrainerAssignmentUpdateDTO.builder().traineeUsername(username).trainerUsernames(request.getTrainerUsernames()).build();
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
        CreatedTrainer created = trainerService.createTrainer(dto);
        TrainerResponseDTO responseDTO = trainerMapper.toDto(created.trainer()).toBuilder().password(created.rawPassword()).build();
        return trainerRestMapper.toRest(responseDTO);
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

    public void changeTraineePassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    @Authenticated
    public void changeTrainerPassword(String username, String oldPassword, String newPassword) throws AuthenticationException {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    @Authenticated
    public TrainerUpdateResponse updateTrainer(TrainerUpdateRequest request, String username) {
        TrainerUpdateDTO dto = trainerRestMapper.toDto(username, request);
        TrainerResponseDTO trainerResponseDTO = trainerService.updateTrainer(dto);

        return trainerRestMapper.toRestUpdateResponse(trainerResponseDTO);
    }

    @Authenticated
    public void changePassword(LoginChangeRequest request) {
        PasswordChangeRequest requestDTO = PasswordChangeRequest.builder().username(request.getUsername()).oldPassword(request.getOldPassword())
                .newPassword(request.getNewPassword())
                .build();

        userProfileService.changePassword(requestDTO);
    }

    public void login(LoginRequest request) {
        User user = userProfileService.login(request);

        sessionContext.setAuthenticatedUser(user);
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
