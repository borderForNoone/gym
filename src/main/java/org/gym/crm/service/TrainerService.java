package org.gym.crm.service;

import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainerUpdateDTO;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TrainerTrainingFilter;

import javax.naming.AuthenticationException;
import java.util.List;

public interface TrainerService {
    TrainerResponseDTO createTrainer(TrainerRequestDTO request);

    TrainerResponseDTO updateTrainer(TrainerUpdateDTO request);

    TrainerInfoDTO getTrainerByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException;

    Trainer updateProfile(String username, Trainer updatedData);

    void setActive(String username, boolean active);

    List<Training> getTrainings(TrainerTrainingFilter filter);

    List<TrainerInfoDTO> getNotAssignedToTrainee(String traineeUsername);
}
