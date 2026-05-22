package org.gym.crm.search.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TrainingFilter;
import org.gym.crm.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TrainingCriteriaBuilder {
    public CriteriaQuery<Training> build(CriteriaBuilder cb, TrainingFilter filter) {
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> root = cq.from(Training.class);
        List<Predicate> predicates = new ArrayList<>();

        Join<?, ?> mainUserJoin = (Join<?, ?>) root
                .fetch(getMainJoinType(), JoinType.LEFT)
                .fetch("user", JoinType.LEFT);
        Join<?, ?> oppositeUserJoin = (Join<?, ?>) root
                .fetch(getOppositeJoinType(), JoinType.LEFT)
                .fetch("user", JoinType.LEFT);

        addUsernamePredicate(cb, mainUserJoin, filter, predicates);
        addFullNamePredicate(cb, oppositeUserJoin, filter, predicates);
        addDateRangePredicate(cb, root, filter, predicates);
        addSpecificPredicates(cb, root, filter, predicates);

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);

        return cq;
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
        Validator.validateNotBlank(username, "Username");

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

    private Optional<Predicate> likePredicate(CriteriaBuilder cb, Expression<String> field, String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(cb.like(cb.lower(field), "%" + value.toLowerCase() + "%"));
    }
}
