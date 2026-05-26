package com.gym.crm.search.criteria;

import com.gym.crm.model.Training;
import com.gym.crm.util.TestTrainingFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class TrainingCriteriaBuilderTest {
    @PersistenceContext
    private EntityManager entityManager;

    private final TrainingCriteriaBuilder builder = new TrainingCriteriaBuilder() {
        @Override
        protected String getMainJoinType() {
            return "trainee";
        }

        @Override
        protected String getOppositeJoinType() {
            return "trainer";
        }
    };

    private CriteriaBuilder criteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    @Test
    void build_shouldCreateQuery() {
        TestTrainingFilter filter = TestTrainingFilter.builder().username("tom").joinFullName("tom tomas").fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .build();

        CriteriaQuery<Training> query = builder.build(criteriaBuilder(), filter);

        assertThat(query).isNotNull();
        assertThat(query.getRestriction()).isNotNull();
        assertThat(query.isDistinct()).isTrue();
    }

    @Test
    void build_shouldThrowException_whenUsernameIsBlank() {
        TestTrainingFilter filter = TestTrainingFilter.builder().username("").build();

        assertThrows(RuntimeException.class, () -> builder.build(criteriaBuilder(), filter));
    }

    @Test
    void build_shouldAddDateRangePredicates() {
        TestTrainingFilter filter = TestTrainingFilter.builder().username("tom").fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31)).build();

        CriteriaQuery<Training> query = builder.build(criteriaBuilder(), filter);

        assertThat(query.getRestriction()).isNotNull();
    }

    @Test
    void build_shouldIgnoreNullFullName() {
        TestTrainingFilter filter = TestTrainingFilter.builder().username("tom").joinFullName(null).build();

        CriteriaQuery<Training> query = builder.build(criteriaBuilder(), filter);

        assertThat(query).isNotNull();
    }

    @Test
    void build_shouldAcceptFullName() {
        TestTrainingFilter filter = TestTrainingFilter.builder().username("tom").joinFullName("tom tomas").build();

        CriteriaQuery<Training> query = builder.build(criteriaBuilder(), filter);

        assertThat(query).isNotNull();
    }
}