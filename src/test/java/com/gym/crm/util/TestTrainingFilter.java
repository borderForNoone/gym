package com.gym.crm.util;

import com.gym.crm.search.filter.TrainingFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TestTrainingFilter extends TrainingFilter {
}