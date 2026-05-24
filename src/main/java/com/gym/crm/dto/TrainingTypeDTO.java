package com.gym.crm.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class TrainingTypeDTO {
    private Long id;
    private String trainingTypeName;
}
