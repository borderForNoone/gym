package org.gym.crm.dto.trainer;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class TrainerResponseDTO {
    private final Long userId;
    private final String firstName;
    private final String lastName;
    private final String username;
    @ToString.Exclude
    private final String password;
    private final String specialization;
    private final Boolean isActive;
}
