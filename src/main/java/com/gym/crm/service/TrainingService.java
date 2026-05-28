package com.gym.crm.service;

import com.gym.crm.facade.dto.TrainingResponseDTO;
import com.gym.crm.facade.dto.TrainingTypeDTO;
import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingService {
    Training create(Training training);

    List<TrainingResponseDTO> getTraineeTrainings(TraineeTrainingFilter filter);

    List<TrainingResponseDTO> getTrainerTrainings(TrainerTrainingFilter filter);

    List<TrainingTypeDTO> getAllTrainingTypes();
}
