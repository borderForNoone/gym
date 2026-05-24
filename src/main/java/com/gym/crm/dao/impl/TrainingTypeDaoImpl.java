package com.gym.crm.dao.impl;

import lombok.RequiredArgsConstructor;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.model.TrainingType;
import com.gym.crm.util.Validator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private final SessionFactory sessionFactory;

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Optional<TrainingType> findByTrainingTypeName(String name) {
        Validator.validateNotBlank(name, "Training Type Name");

        String hql = """
                SELECT t FROM TrainingType t
                WHERE t.trainingTypeName = :name
                """;

        return session().createQuery(hql, TrainingType.class).setParameter("name", name).getResultList().stream().findFirst();
    }

    @Override
    public List<TrainingType> findAll() {
        return session().createQuery("FROM TrainingType", TrainingType.class).getResultList();
    }
}
