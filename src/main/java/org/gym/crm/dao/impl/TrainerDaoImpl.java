package org.gym.crm.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.config.TransactionManager;
import org.gym.crm.dao.TrainerDao;
import org.gym.crm.model.Trainer;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainerDaoImpl implements TrainerDao {
    private static final String USERNAME = "username";
    private static final String USERNAME_LABEL = "Username";
    private static final String TRAINER_LABEL = "Trainer";
    private static final String TRAINEE_USERNAME_LABEL = "Trainee Username";
    private static final String FIND_BY_USERNAME_QUERY = """
            FROM Trainer t
            JOIN FETCH t.user
            WHERE t.user.username = :username
            """;
    private static final String FIND_NOT_ASSIGNED_QUERY = """
            SELECT t FROM Trainer t
            WHERE t.id NOT IN (
               SELECT tr.trainer.id FROM Training tr
               WHERE tr.trainee.user.username = :username
            )
            """;
    private static final String EXISTS_BY_USERNAME_QUERY = """
            SELECT COUNT(t) FROM Trainer t
            JOIN t.user u
            WHERE u.username = :username
            """;

    private final TransactionManager transactionManager;

    @Override
    public Trainer save(Trainer trainer) {
        Validator.validateNotNull(trainer, TRAINER_LABEL);

        transactionManager.performWithinTx(manager -> manager.persist(trainer));

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Validator.validateId(id);

        return transactionManager.performReturningWithinTx(manager ->
                Optional.ofNullable(manager.find(Trainer.class, id))
        );
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        return transactionManager.performReturningWithinTx(manager ->
                manager.createQuery(FIND_BY_USERNAME_QUERY, Trainer.class)
                        .setParameter(USERNAME, username)
                        .getResultStream()
                        .findFirst()
        );
    }

    @Override
    public List<Trainer> findAll() {
        return transactionManager.performReturningWithinTx(manager ->
                manager.createQuery("FROM Trainer", Trainer.class)
                        .getResultList()
        );
    }

    @Override
    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        Validator.validateNotBlank(traineeUsername, TRAINEE_USERNAME_LABEL);

        return transactionManager.performReturningWithinTx(manager ->
                manager.createQuery(FIND_NOT_ASSIGNED_QUERY, Trainer.class)
                        .setParameter(USERNAME, traineeUsername)
                        .getResultList()
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        return transactionManager.performReturningWithinTx(manager ->
                manager.createQuery(EXISTS_BY_USERNAME_QUERY, Long.class)
                        .setParameter(USERNAME, username)
                        .getSingleResult() > 0
        );
    }

    @Override
    public Trainer update(Trainer trainer) {
        Validator.validateId(trainer.getId());

        transactionManager.performWithinTx(manager -> manager.merge(trainer));

        return trainer;
    }
}
