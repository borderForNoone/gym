package com.gym.crm.facade.dto;

import com.gym.crm.model.TrainingType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Tolerate;

@Getter
@ToString
@EqualsAndHashCode
@Builder
public class TrainerInfoDTO {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final Boolean isActive;
    private final String specialization;

    @Tolerate
    public TrainerInfoDTO(String username, String firstName, String lastName, TrainingType specialization) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization.getTrainingTypeName();
        this.isActive = null;
    }
}