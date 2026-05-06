package org.gym.crm.service;

import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TraineeTrainingFilter;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(Trainee trainee);

    Optional<Trainee> findById(Long id);

    List<Trainee> findAll();

    Trainee update(Trainee trainee);

    void delete(Long id);

    boolean authenticate(String username, String password);

    Optional<Trainee> findByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException;

    Trainee setActive(String username, boolean active);

    void deleteByUsername(String username);

    List<Training> getTrainings(TraineeTrainingFilter filter);

    List<Trainer> getUnassignedTrainers(String traineeUsername);

    Trainee updateTrainers(String traineeUsername, List<String> trainerUsernames);

    Trainee updateProfile(String username, Trainee updatedData);
}
