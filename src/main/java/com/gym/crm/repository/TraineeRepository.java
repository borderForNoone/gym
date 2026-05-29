package com.gym.crm.repository;

import com.gym.crm.model.Trainee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    @EntityGraph(attributePaths = {"user", "trainers"})
    Optional<Trainee> findByUser_Username(String username);

    boolean existsByUser_Username(String username);

    double countByUserIsActive(boolean isActive);
}
