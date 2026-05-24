package com.gym.crm.service;

import com.gym.crm.dto.CreatedTrainer;
import com.gym.crm.dto.TrainerInfoDTO;
import com.gym.crm.dto.TrainerRequestDTO;
import com.gym.crm.dto.TrainerResponseDTO;
import com.gym.crm.dto.TrainerUpdateDTO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TrainerTrainingFilter;

import javax.naming.AuthenticationException;
import java.util.List;

public interface TrainerService {
    CreatedTrainer createTrainer(TrainerRequestDTO request);

    TrainerResponseDTO updateTrainer(TrainerUpdateDTO request);

    TrainerInfoDTO getTrainerByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException;

    Trainer updateProfile(String username, Trainer updatedData);

    void setActive(String username, boolean active);

    List<Training> getTrainings(TrainerTrainingFilter filter);

    List<TrainerInfoDTO> getNotAssignedToTrainee(String traineeUsername);
}
