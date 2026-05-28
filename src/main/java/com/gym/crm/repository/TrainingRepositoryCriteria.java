package com.gym.crm.repository;

import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingRepositoryCriteria {
    List<Training> findByTraineeCriteria(TraineeTrainingFilter filter);

    List<Training> findByTrainerCriteria(TrainerTrainingFilter filter);
}