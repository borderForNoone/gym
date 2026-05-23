package org.gym.crm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TraineeDaoImpl implements TraineeDao {
    private static final String USERNAME = "username";
    private static final String USERNAME_LABEL = "Username";
    private static final String TRAINEE_LABEL = "Trainee";
    private static final String TRAINEE_USERNAME_LABEL = "Trainee username";
    private static final String FIND_TRAINEE_BY_USERNAME_QUERY = """
            SELECT t FROM Trainee t
            JOIN FETCH t.user u
            LEFT JOIN FETCH t.trainers
            WHERE u.username = :username
            """;
    private static final String FIND_UNASSIGNED_TRAINERS_QUERY = """
            SELECT tr FROM Trainer tr
            JOIN FETCH tr.user u
            WHERE tr.id NOT IN (
                SELECT t.id FROM Trainee tn
                JOIN tn.trainers t
                WHERE tn.user.username = :username
            )
            """;
    private static final String FIND_TRAINEE_WITH_TRAINERS_QUERY = """
            SELECT t FROM Trainee t
            JOIN FETCH t.user u
            LEFT JOIN FETCH t.trainers
            WHERE u.username = :username
            """;
    private static final String FIND_TRAINERS_BY_USERNAMES_QUERY = """
            SELECT tr FROM Trainer tr
            JOIN FETCH tr.user u
            WHERE u.username IN :usernames
            """;
    private static final String FIND_TRAINEE_BY_USERNAME_FETCH_QUERY = """
                SELECT t FROM Trainee t
                JOIN FETCH t.user u
                WHERE u.username = :username
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainee save(Trainee trainee) {
        Validator.validateNotNull(trainee, TRAINEE_LABEL);

        entityManager.persist(trainee);

        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Validator.validateId(trainee.getId());

        return entityManager.merge(trainee);
    }

    @Override
    public void delete(Trainee trainee) {
        Validator.validateNotNull(trainee, "Trainee");
        Validator.validateId(trainee.getId());

        Trainee managed = entityManager.contains(trainee) ? trainee : entityManager.merge(trainee);

        entityManager.remove(managed);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        return entityManager.createQuery(FIND_TRAINEE_BY_USERNAME_QUERY, Trainee.class).setParameter(USERNAME, username).getResultStream().findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        Long count = entityManager.createQuery("""
                        SELECT COUNT(t) FROM Trainee t
                        JOIN t.user u
                        WHERE u.username = :username
                        """, Long.class)
                .setParameter(USERNAME, username)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteByUsername(String username) {
        Validator.validateNotBlank(username, USERNAME_LABEL);

        Trainee trainee = entityManager.createQuery(
                        "SELECT t FROM Trainee t JOIN FETCH t.user u WHERE u.username = :username",
                        Trainee.class)
                .setParameter(USERNAME, username)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (trainee != null) {
            entityManager.remove(trainee);
        }
    }

    @Override
    public List<Trainer> findUnassignedTrainers(String traineeUsername) {
        Validator.validateNotBlank(traineeUsername, TRAINEE_USERNAME_LABEL);

        return entityManager.createQuery(FIND_UNASSIGNED_TRAINERS_QUERY, Trainer.class)
                .setParameter(USERNAME, traineeUsername)
                .getResultList();
    }

    @Override
    public void updateTrainersList(String username, List<Trainer> trainers) {
        Validator.validateNotBlank(username, USERNAME_LABEL);
        Validator.validateNotNull(trainers, "Trainers");

        Trainee trainee = entityManager.createQuery(
                        "SELECT t FROM Trainee t JOIN FETCH t.user u LEFT JOIN FETCH t.trainers WHERE u.username = :username",
                        Trainee.class)
                .setParameter(USERNAME, username)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));

        List<Trainer> managedTrainers = trainers.stream()
                .map(tr -> entityManager.find(Trainer.class, tr.getId()))
                .filter(Objects::nonNull)
                .toList();

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(managedTrainers);

        entityManager.merge(trainee);
    }

    @Override
    public List<Trainer> findAllByUsernames(List<String> trainerUsernames) {
        Validator.validateNotNull(trainerUsernames, "Trainer usernames");

        if (trainerUsernames.isEmpty()) {
            return List.of();
        }

        return entityManager.createQuery(FIND_TRAINERS_BY_USERNAMES_QUERY, Trainer.class)
                .setParameter("usernames", trainerUsernames)
                .getResultList();
    }
}