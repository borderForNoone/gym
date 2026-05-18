package org.gym.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class AuthRequestDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 110, message = "Username must be between 3 and 110 characters long")
    String username;

    @NotBlank(message = "New password is required")
    @Size(min = 10, max = 100, message = "Password must be between 10 and 100 characters long")
    @ToString.Exclude
    String password;
}
