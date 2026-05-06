package org.gym.crm.service;

import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TrainerTrainingFilter;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(Trainer trainer);

    Optional<Trainer> findById(Long id);

    List<Trainer> findAll();

    Trainer update(Trainer trainer);

    boolean authenticate(String username, String password);

    Optional<Trainer> findByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException;

    Trainer updateProfile(String username, Trainer updatedData);

    void setActive(String username, boolean active);

    List<Training> getTrainings(TrainerTrainingFilter filter);
}
