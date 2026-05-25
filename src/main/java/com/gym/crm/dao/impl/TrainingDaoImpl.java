package com.gym.crm.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.util.Validator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainingDaoImpl implements TrainingDao {
    private final SessionFactory sessionFactory;

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Training save(Training training) {
        Validator.validateNotNull(training, "Training");

        session().persist(training);

        return training;
    }

    @Override
    public List<Training> findByTraineeCriteria(TraineeTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        String hql = """
                SELECT t FROM Training t
                JOIN t.trainee tr
                JOIN tr.user u
                JOIN t.trainer tn
                WHERE (:username IS NULL OR u.username = :username)
                AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
                AND (:toDate IS NULL OR t.trainingDate <= :toDate)
                """;

        return session().createQuery(hql, Training.class).setParameter("username", filter.getUsername()).setParameter("fromDate", filter.getFromDate())
                .setParameter("toDate", filter.getToDate())
                .getResultList();
    }

    @Override
    public List<Training> findByTrainerCriteria(TrainerTrainingFilter filter) {
        Validator.validateNotNull(filter, "Filter");

        String hql = """
                SELECT t FROM Training t
                JOIN t.trainer tr
                JOIN tr.user u
                WHERE (:username IS NULL OR u.username = :username)
                AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
                AND (:toDate IS NULL OR t.trainingDate <= :toDate)
                """;

        return session().createQuery(hql, Training.class).setParameter("username", filter.getUsername()).setParameter("fromDate", filter.getFromDate())
                .setParameter("toDate", filter.getToDate())
                .getResultList();
    }
}