package org.gym.crm.service.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.User;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainerServiceImpl implements TrainerService {
    @Autowired
    @Setter
    private TrainerDao trainerDao;

    @Autowired
    private UserProfileService userProfileService;

    @Override
    public Trainer create(Trainer trainer) {
        log.info("Creating trainer: {} {}", trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        String username = userProfileService.generateUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        String password = userProfileService.generatePassword();

        User user = trainer.getUser().toBuilder()
                .username(username)
                .password(password)
                .build();

        Trainer trainerWithProfile = trainer.toBuilder()
                .user(user)
                .build();

        Trainer saved = trainerDao.save(trainerWithProfile);

        log.info("Trainer created successfully with username={}", username);
        return saved;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Searching trainer by id={}", id);
        return trainerDao.findById(id);
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Fetching all trainers");
        return trainerDao.findAll();
    }

    @Override
    public Trainer update(Trainer trainer) {
        log.info("Updating trainer with id={}", trainer.getId());
        return trainerDao.update(trainer);
    }
}
