package org.gym.crm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TrainingDao;
import org.gym.crm.model.Training;
import org.gym.crm.search.criteria.TraineeTrainingCriteriaBuilder;
import org.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainingDaoImpl implements TrainingDao {
    @PersistenceContext
    private EntityManager entityManager;

    private final TraineeTrainingCriteriaBuilder traineeCriteriaBuilder;
    private final TrainerTrainingCriteriaBuilder trainerCriteriaBuilder;

    @Override
    public Training save(Training training) {
        Validator.validateNotNull(training, "Training");

        entityManager.persist(training);

        return training;
    }

    @Override
    public List<Training> findByTraineeCriteria(TraineeTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = traineeCriteriaBuilder.build(cb, filter);

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Training> findByTrainerCriteria(TrainerTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = trainerCriteriaBuilder.build(cb, filter);

        return entityManager.createQuery(cq).getResultList();
    }
}
