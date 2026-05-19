package org.gym.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class TrainerAssignmentUpdateDTO {
    @NotBlank(message = "Trainee username is required")
    private final String traineeUsername;

    @NotEmpty
    private final List<String> trainerUsernames;
}
