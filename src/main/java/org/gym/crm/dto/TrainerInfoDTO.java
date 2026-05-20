package org.gym.crm.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class TrainerInfoDTO {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final Boolean isActive;
    private final String specialization;
}
