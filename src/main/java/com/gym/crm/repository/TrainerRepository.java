package com.gym.crm.repository;

import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUser_Username(String username);

    boolean existsByUser_Username(String username);

    List<Trainer> findByUser_UsernameNotIn(List<String> usernames);

    List<Trainer> findByUser_UsernameIn(List<String> usernames);

    List<Trainer> findByIdNotIn(List<Long> ids);

    @Query("""
            select new com.gym.crm.facade.dto.TrainerInfoDTO(
                t.user.username,
                t.user.firstName,
                t.user.lastName,
                t.specialization
            )
            from Trainer t
            where not exists (
                select 1
                from t.trainees tr
                where tr.user.username = :traineeUsername
            )
            """)
    List<TrainerInfoDTO> findAllNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);
}
