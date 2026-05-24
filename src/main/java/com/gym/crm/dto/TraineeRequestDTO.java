package com.gym.crm.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Getter
@Builder
@Jacksonized
@ToString
@EqualsAndHashCode
public class TraineeRequestDTO {
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String address;
}
