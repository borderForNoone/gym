package com.gym.crm.dao;


import com.gym.crm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {
    Optional<TrainingType> findByTrainingTypeName(String name);

    List<TrainingType> findAll();
}