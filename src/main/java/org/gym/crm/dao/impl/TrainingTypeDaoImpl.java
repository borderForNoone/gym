package org.gym.crm.dao.impl;

import lombok.RequiredArgsConstructor;
import org.gym.crm.config.TransactionManager;
import org.gym.crm.dao.TrainingTypeDao;
import org.gym.crm.model.TrainingType;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private final TransactionManager transactionManager;

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
}
