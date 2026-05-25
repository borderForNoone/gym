package com.gym.crm.repository;

import com.gym.crm.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    @Query("""
                SELECT t FROM Training t
                JOIN t.trainee tr
                JOIN tr.user u
                JOIN t.trainer tn
                WHERE (:username IS NULL OR u.username = :username)
                AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
                AND (:toDate IS NULL OR t.trainingDate <= :toDate)
            """)
    List<Training> findByTraineeCriteria(
            @Param("username") String username,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
                SELECT t FROM Training t
                JOIN t.trainer tr
                JOIN tr.user u
                WHERE (:username IS NULL OR u.username = :username)
                AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
                AND (:toDate IS NULL OR t.trainingDate <= :toDate)
            """)
    List<Training> findByTrainerCriteria(
            @Param("username") String username,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
                SELECT t FROM Training t
                JOIN t.trainee tr
                JOIN tr.user u
                WHERE (:username IS NULL OR u.username = :username)
                AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
                AND (:toDate IS NULL OR t.trainingDate <= :toDate)
            """)
    List<Training> findTraineeTrainings(
            @Param("username") String username,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
