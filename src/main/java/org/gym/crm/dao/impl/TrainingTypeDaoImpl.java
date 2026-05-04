package org.gym.crm.dao.impl;

import lombok.RequiredArgsConstructor;
import org.gym.crm.config.TransactionManager;
import org.gym.crm.dao.TrainingTypeDao;
import org.gym.crm.model.TrainingType;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private final TransactionManager transactionManager;

    @Override
    public Optional<TrainingType> findById(Long id) {
        Validator.validateId(id);

        return transactionManager.performReturningWithinTx(manager ->
                Optional.ofNullable(manager.find(TrainingType.class, id)));
    }

    @Override
    public Optional<TrainingType> findByTrainingTypeName(String name) {
        Validator.validateNotBlank(name, "Training Type Name");

        return transactionManager.performReturningWithinTx(manager -> manager
                .createQuery("SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
        );
    }

    @Override
    public List<TrainingType> findAll() {
        return transactionManager.performReturningWithinTx(manager -> manager
                .createQuery("from TrainingType", TrainingType.class)
                .getResultList()
        );
    }
}
