package org.gym.crm.service;

import org.gym.crm.dto.TraineeInfoDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.dto.TraineeUpdateDTO;
import org.gym.crm.dto.TrainerAssignmentUpdateDTO;
import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TraineeTrainingFilter;

import javax.naming.AuthenticationException;
import java.util.List;

public interface TraineeService {
    Trainee create(Trainee trainee);

    TraineeResponseDTO update(TraineeUpdateDTO trainee);

    TraineeInfoDTO getTraineeByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException;

    Trainee setActive(String username, boolean active);

    void deleteByUsername(String username);

    List<Training> getTrainings(TraineeTrainingFilter filter);

    List<Trainer> getUnassignedTrainers(String traineeUsername);

    List<TrainerInfoDTO> updateTrainersList(TrainerAssignmentUpdateDTO dto);

    Trainee updateProfile(String username, Trainee updatedData);
}
