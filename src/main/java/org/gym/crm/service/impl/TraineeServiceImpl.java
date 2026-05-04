package org.gym.crm.service.impl;

import org.gym.crm.dao.TraineeDao;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.model.Trainee;
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.UserProfileService;
import org.gym.crm.util.Validator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraineeServiceImpl implements TraineeService {
    private static final String TRAINEE_NOT_FOUND_BY_ID = "Trainee not found by id: %s";
    private static final String TRAINEE = "Trainee";

    @Setter(onMethod_ = {@Autowired})
    private TraineeDao dao;

    @Setter(onMethod_ = {@Autowired})
    private UserProfileService userCredentialGenerator;

    @Setter(onMethod_ = {@Autowired})
    private PasswordEncoder passwordEncoder;

    @Override
    public Trainee create(Trainee trainee) {
        Validator.validateNotNull(trainee, TRAINEE);

        log.info("Creating trainee: firstName={} lastName{}", trainee.getUser().getFirstName(), trainee.getUser().getLastName());

        String username = userCredentialGenerator.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName());
        String rawPassword = userCredentialGenerator.generatePassword();

        Trainee withCredentials = trainee.toBuilder()
                .user(
                        trainee.getUser().toBuilder()
                                .username(username)
                                .password(passwordEncoder.encode(rawPassword))
                                .isActive(true)
                                .build()
                )
                .build();

        Trainee saved = dao.save(withCredentials);
        log.info("Trainee created successfully: username={}", saved.getUser().getUsername());

        return saved;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.of(dao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAINEE_NOT_FOUND_BY_ID, id))));
    }

    @Override
    public List<Trainee> findAll() {
        log.info("Fetching all trainees");

        List<Trainee> trainees = dao.findAll();

        log.info("Fetched {} trainees", trainees.size());
        return trainees;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Validator.validateNotNull(trainee, TRAINEE);

        log.info("Updating trainee: id={}", trainee.getId());
        findById(trainee.getId());

        Trainee updated = dao.update(trainee);
        log.info("Trainee updated successfully: id={}", updated.getId());

        return updated;
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting trainee: id={}", id);
        findById(id);

        dao.delete(id);
        log.info("Trainee deleted successfully: id={}", id);
    }
}
