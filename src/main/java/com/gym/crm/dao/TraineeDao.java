package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);

    Trainee update(Trainee trainee);

    void delete(Trainee trainee);

    Optional<Trainee> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);

    List<Trainer> findUnassignedTrainers(String traineeUsername);

    void updateTrainersList(String traineeUsername, List<Trainer> trainers);

    List<Trainer> findAllByUsernames(List<String> trainerUsernames);
}
