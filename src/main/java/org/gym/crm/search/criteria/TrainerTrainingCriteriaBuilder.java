package org.gym.crm.search.criteria;

import org.springframework.stereotype.Component;

@Component
public class TrainerTrainingCriteriaBuilder extends TrainingCriteriaBuilder {

    @Override
    protected String getMainJoinType() {
        return "trainer";
    }

    @Override
    protected String getOppositeJoinType() {
        return "trainee";
    }
}
