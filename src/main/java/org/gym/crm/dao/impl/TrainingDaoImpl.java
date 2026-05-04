package org.gym.crm.dao.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.config.TransactionManager;
import org.gym.crm.dao.TrainingDao;
import org.gym.crm.model.Training;
import org.gym.crm.search.criteria.TraineeTrainingCriteriaBuilder;
import org.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainingDaoImpl implements TrainingDao {
    private final TransactionManager transactionManager;
    private final TraineeTrainingCriteriaBuilder traineeCriteriaBuilder;
    private final TrainerTrainingCriteriaBuilder trainerCriteriaBuilder;

    @Override
    public Training save(Training training) {
        Validator.validateNotNull(training, "Training");

        transactionManager.performWithinTx(manager -> manager.persist(training));

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Validator.validateId(id);

        return transactionManager.performReturningWithinTx(manager ->
                Optional.ofNullable(manager.find(Training.class, id)));
    }

    @Override
    public List<Training> findAll() {
        return transactionManager.performReturningWithinTx(manager -> manager
                .createQuery("from Training", Training.class)
                .getResultList()
        );
    }

    @Override
    public List<Training> findByTraineeCriteria(TraineeTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        return transactionManager.performReturningWithinTx(manager -> {
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Training> cq = traineeCriteriaBuilder.build(cb, filter);

            return manager.createQuery(cq).getResultList();
        });
    }

    @Override
    public List<Training> findByTrainerCriteria(TrainerTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        return transactionManager.performReturningWithinTx(manager -> {
            CriteriaBuilder cb = manager.getCriteriaBuilder();
            CriteriaQuery<Training> cq = trainerCriteriaBuilder.build(cb, filter);

            return manager.createQuery(cq).getResultList();
        });
    }
}
