package org.gym.crm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.gym.crm.dao.TrainingTypeDao;
import org.gym.crm.model.TrainingType;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findByTrainingTypeName(String name) {
        Validator.validateNotBlank(name, "Training Type Name");

        return entityManager.createQuery("SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<TrainingType> findAll() {
        return entityManager.createQuery("FROM TrainingType", TrainingType.class).getResultList();
    }
}
