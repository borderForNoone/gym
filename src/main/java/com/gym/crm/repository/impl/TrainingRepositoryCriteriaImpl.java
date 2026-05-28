package com.gym.crm.repository.impl;

import com.gym.crm.exception.ValidationFailedException;
import com.gym.crm.model.Training;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingRepositoryCriteria;
import com.gym.crm.search.criteria.TraineeTrainingCriteriaBuilder;
import com.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class TrainingRepositoryCriteriaImpl implements TrainingRepositoryCriteria {
    private static final String FILTER_NOT_NULL = "Filter cannot be null";

    private final TrainingRepository repository;
    private final TraineeTrainingCriteriaBuilder traineeCriteriaBuilder;
    private final TrainerTrainingCriteriaBuilder trainerCriteriaBuilder;

    @Override
    public List<Training> findByTraineeCriteria(TraineeTrainingFilter filter) {
        if (Objects.isNull(filter)) {
            throw new ValidationFailedException(FILTER_NOT_NULL);
        }
        Specification<Training> specification = traineeCriteriaBuilder.build(filter);

        return repository.findAll(specification);
    }

    @Override
    public List<Training> findByTrainerCriteria(TrainerTrainingFilter filter) {
        if (Objects.isNull(filter)) {
            throw new ValidationFailedException(FILTER_NOT_NULL);
        }
        Specification<Training> specification = trainerCriteriaBuilder.build(filter);

        return repository.findAll(specification);
    }
}