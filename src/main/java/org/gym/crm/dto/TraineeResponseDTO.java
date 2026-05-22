package org.gym.crm.dto;

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
public class TraineeResponseDTO {
    private final Long userId;
    private final String firstName;
    private final String lastName;
    private final String username;
    @ToString.Exclude
    private final String password;
    private final LocalDate dateOfBirth;
    private final String address;
    private final Boolean isActive;
}
