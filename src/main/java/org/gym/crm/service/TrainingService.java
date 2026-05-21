package org.gym.crm.service;

import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.dto.TrainingTypeDTO;
import org.gym.crm.model.Training;
import org.gym.crm.rest.TrainingTypeResponse;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training create(Training training);

    List<TrainingResponseDTO> getTraineeTrainings(TraineeTrainingFilter filter);

    List<TrainingResponseDTO> getTrainerTrainings(TrainerTrainingFilter filter);

    List<TrainingTypeDTO> getAllTrainingTypes();
}
