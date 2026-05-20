package org.gym.crm.service;

import org.gym.crm.dto.PasswordChangeRequest;
import org.gym.crm.dto.ToggleActiveRequestDTO;
import org.gym.crm.model.User;
import org.gym.crm.rest.LoginRequest;

public interface UserProfileService {
    String generateUsername(String firstName, String lastName);

    String generatePassword();

    Boolean authenticate(String username, String password);

    void changePassword(PasswordChangeRequest requestDTO);

    User login(LoginRequest request);

    void toggleActive(ToggleActiveRequestDTO request);
}
