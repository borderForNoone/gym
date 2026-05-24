package com.gym.crm.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class TrainerRequestDTO {
    private final String firstName;
    private final String lastName;
    private final String specialization;
}
