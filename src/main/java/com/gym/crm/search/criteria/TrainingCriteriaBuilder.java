package com.gym.crm.search.criteria;

import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TrainingFilter;
import com.gym.crm.service.common.CoreValidator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TrainingCriteriaBuilder {
    @Autowired
    private CoreValidator coreValidator;

    public CriteriaQuery<Training> build(CriteriaBuilder criteriaBuilder, TrainingFilter filter) {
        CriteriaQuery<Training> criteriaQuery = criteriaBuilder.createQuery(Training.class);
        Root<Training> root = criteriaQuery.from(Training.class);
        List<Predicate> predicates = new ArrayList<>();

        Join<?, ?> mainUserJoin = (Join<?, ?>) root
                .fetch(getMainJoinType(), JoinType.LEFT)
                .fetch("user", JoinType.LEFT);
        Join<?, ?> oppositeUserJoin = (Join<?, ?>) root
                .fetch(getOppositeJoinType(), JoinType.LEFT)
                .fetch("user", JoinType.LEFT);

        addUsernamePredicate(criteriaBuilder, mainUserJoin, filter, predicates);
        addFullNamePredicate(criteriaBuilder, oppositeUserJoin, filter, predicates);
        addDateRangePredicate(criteriaBuilder, root, filter, predicates);
        addSpecificPredicates(criteriaBuilder, root, filter, predicates);

        criteriaQuery.where(predicates.toArray(new Predicate[0]));
        criteriaQuery.distinct(true);

        return criteriaQuery;
    }

    protected abstract String getMainJoinType();

    protected abstract String getOppositeJoinType();

    protected void addSpecificPredicates(CriteriaBuilder criteriaBuilder, Root<Training> root, TrainingFilter filter, List<Predicate> predicates) {
    }

    private void addDateRangePredicate(CriteriaBuilder criteriaBuilder, Root<Training> root, TrainingFilter filter, List<Predicate> predicates) {
        Optional.ofNullable(filter.getFromDate()).ifPresent(fromDate -> predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("trainingDate"), fromDate)));
        Optional.ofNullable(filter.getToDate()).ifPresent(toDate -> predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("trainingDate"), toDate)));
    }

    private void addUsernamePredicate(CriteriaBuilder criteriaBuilder, Join<?, ?> join, TrainingFilter filter, List<Predicate> predicates) {
        String username = filter.getUsername();
        coreValidator.validateNotBlank(username, "Username");

        predicates.add(criteriaBuilder.equal(join.get("username"), username));
    }

    private void addFullNamePredicate(CriteriaBuilder criteriaBuilder, Join<?, ?> join, TrainingFilter filter, List<Predicate> predicates) {
        String fullName = filter.getJoinFullName();

        if (fullName == null || fullName.isBlank()) {
            return;
        }

        Expression<String> concatenated = criteriaBuilder.concat(criteriaBuilder.concat(criteriaBuilder.lower(join.get("firstName")), " "),
                criteriaBuilder.lower(join.get("lastName")));

        predicates.add(criteriaBuilder.equal(concatenated, fullName.trim().toLowerCase()));
    }
}
