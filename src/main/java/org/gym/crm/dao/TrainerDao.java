package org.gym.crm.dao;

import org.gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer save(Trainer trainer);

    Trainer update(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findNotAssignedToTrainee(String traineeUsername);

    boolean existsByUsername(String username);
}
