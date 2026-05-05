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
    private final TransactionManager transactionManager;

    @Override
    public Trainer save(Trainer trainer) {
        Validator.validateNotNull(trainer, "Trainer");

        transactionManager.performWithinTx(manager -> manager.persist(trainer));

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Validator.validateId(id);

        return transactionManager.performReturningWithinTx(manager ->
                Optional.ofNullable(manager.find(Trainer.class, id)));
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Validator.validateNotBlank(username, "Username");

        return transactionManager.performReturningWithinTx(manager ->
                manager.createQuery("FROM Trainer t JOIN FETCH t.user WHERE t.user.username = :username", Trainer.class)
                        .setParameter("username", username)
                        .getResultStream()
                        .findFirst()
        );
    }

    @Override
    public List<Trainer> findAll() {
        return transactionManager.performReturningWithinTx(manager -> manager
                .createQuery("from Trainer", Trainer.class)
                .getResultList()
        );
    }

    @Override
    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        Validator.validateNotBlank(traineeUsername, "Trainee Username");

        return transactionManager.performReturningWithinTx(manager ->
                manager.createQuery(
                                "SELECT t FROM Trainer t " +
                                        "WHERE t.id NOT IN (" +
                                        "   SELECT tr.trainer.id FROM Training tr " +
                                        "   WHERE tr.trainee.user.username = :username" +
                                        ")",
                                Trainer.class
                        )
                        .setParameter("username", traineeUsername)
                        .getResultList()
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        Validator.validateNotBlank(username, "Username");

        return transactionManager.performReturningWithinTx(manager -> manager
                .createQuery("""
                        SELECT COUNT(t) FROM Trainer t
                        JOIN t.user u
                        WHERE u.username = :username
                        """, Long.class)
                .setParameter("username", username)
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
