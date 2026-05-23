package org.gym.crm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            SELECT t FROM Trainer t
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

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainer save(Trainer trainer) {
        Validator.validateNotNull(trainer, TRAINER_LABEL);

        entityManager.persist(trainer);
        return trainer;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        return entityManager.createQuery(FIND_BY_USERNAME_QUERY, Trainer.class).setParameter(USERNAME, username).getResultStream().findFirst();
    }

    @Override
    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        Validator.validateNotBlank(traineeUsername, TRAINEE_USERNAME_LABEL);

        return entityManager.createQuery(FIND_NOT_ASSIGNED_QUERY, Trainer.class).setParameter(USERNAME, traineeUsername).getResultList();
    }

    @Override
    public boolean existsByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        Long count = entityManager.createQuery(EXISTS_BY_USERNAME_QUERY, Long.class).setParameter(USERNAME, username).getSingleResult();

        return count > 0;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Validator.validateId(trainer.getId());

        return entityManager.merge(trainer);
    }
}