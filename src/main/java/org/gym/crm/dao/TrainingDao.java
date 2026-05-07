package org.gym.crm.dao;

import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingDao {
    Training save(Training training);

    List<Training> findByTraineeCriteria(TraineeTrainingFilter filter);

    List<Training> findByTrainerCriteria(TrainerTrainingFilter filter);
}
