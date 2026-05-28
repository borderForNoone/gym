package com.gym.crm.search.criteria;

import com.gym.crm.exception.ValidationFailedException;
import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TrainingFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TrainingCriteriaBuilder {
    public Specification<Training> build(TrainingFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<?, ?> mainUserJoin = root.join(getMainJoinType(), JoinType.LEFT)
                    .join("user", JoinType.LEFT);
            Join<?, ?> oppositeUserJoin = root.join(getOppositeJoinType(), JoinType.LEFT)
                    .join("user", JoinType.LEFT);

            addUsernamePredicate(cb, mainUserJoin, filter, predicates);
            addFullNamePredicate(cb, oppositeUserJoin, filter, predicates);
            addDateRangePredicate(cb, root, filter, predicates);
            addSpecificPredicates(cb, root, filter, predicates);

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    protected abstract String getMainJoinType();

    protected abstract String getOppositeJoinType();

    protected void addSpecificPredicates(CriteriaBuilder cb, Root<Training> root, TrainingFilter filter, List<Predicate> predicates) {
    }

    private void addDateRangePredicate(CriteriaBuilder cb, Root<Training> root, TrainingFilter filter, List<Predicate> predicates) {
        Optional.ofNullable(filter.getFromDate())
                .ifPresent(fromDate -> predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate)));
        Optional.ofNullable(filter.getToDate())
                .ifPresent(toDate -> predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate)));
    }

    private void addUsernamePredicate(CriteriaBuilder cb, Join<?, ?> join, TrainingFilter filter, List<Predicate> predicates) {
        String username = filter.getUsername();
        if (username == null || username.isEmpty()) {
            throw new ValidationFailedException("Username cannot be null or empty");
        }

        predicates.add(cb.equal(join.get("username"), username));
    }

    private void addFullNamePredicate(CriteriaBuilder cb, Join<?, ?> join, TrainingFilter filter, List<Predicate> predicates) {
        String fullName = filter.getJoinFullName();

        if (fullName == null || fullName.isBlank()) {
            return;
        }

        Expression<String> concatenated = cb.concat(
                cb.concat(cb.lower(join.get("firstName")), " "),
                cb.lower(join.get("lastName")));

        predicates.add(cb.equal(concatenated, fullName.trim().toLowerCase()));
    }
}