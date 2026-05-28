package com.gym.crm.search.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainingFilter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TraineeTrainingCriteriaBuilder extends TrainingCriteriaBuilder {
    @Override
    protected void addSpecificPredicates(CriteriaBuilder cb, Root<Training> root, TrainingFilter trainingFilter, List<Predicate> predicates) {
        TraineeTrainingFilter filter = (TraineeTrainingFilter) trainingFilter;

        addTrainingTypePredicate(cb, root, filter, predicates);
    }

    @Override
    protected String getMainJoinType() {
        return "trainee";
    }

    @Override
    protected String getOppositeJoinType() {
        return "trainer";
    }

    private void addTrainingTypePredicate(CriteriaBuilder cb, Root<Training> root, TraineeTrainingFilter filter, List<Predicate> predicates) {
        Optional.ofNullable(filter.getTrainingTypeName()).ifPresent(name -> predicates.add(cb.equal(root.get("trainingType").get("trainingTypeName"), name)));
    }
}
