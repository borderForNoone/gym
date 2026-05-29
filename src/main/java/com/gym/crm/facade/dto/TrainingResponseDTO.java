package com.gym.crm.facade.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class TrainingResponseDTO {
    private final Long id;
    private final Long traineeId;
    private final Long trainerId;
    private final String trainingName;
    private final String trainingTypeName;
    private final LocalDate trainingDate;
    private final int trainingDuration;
    private final String traineeUsername;
    private final String trainerUsername;
}
