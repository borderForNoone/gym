package org.gym.crm.search.filter;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TraineeTrainingFilter extends TrainingFilter {
    private String trainingTypeName;
}
