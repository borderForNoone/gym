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
public class TraineeInfoDTO {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final Boolean isActive;
    private final LocalDate dateOfBirth;
    private final String address;
}