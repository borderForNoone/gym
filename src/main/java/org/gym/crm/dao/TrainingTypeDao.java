package org.gym.crm.dao;


import org.gym.crm.model.TrainingType;

import java.util.Optional;

public interface TrainingTypeDao {
    Optional<TrainingType> findByTrainingTypeName(String name);
}