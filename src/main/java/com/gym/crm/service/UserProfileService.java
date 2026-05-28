package com.gym.crm.service;

import com.gym.crm.facade.dto.PasswordChangeRequest;
import com.gym.crm.facade.dto.ToggleActiveRequestDTO;
import com.gym.crm.model.User;
import org.gym.crm.rest.LoginRequest;

public interface UserProfileService {
    String generateUsername(String firstName, String lastName);

    String generatePassword();

    Boolean authenticate(String username, String password);

    void changePassword(PasswordChangeRequest requestDTO);

    User login(LoginRequest request);

    void toggleActive(ToggleActiveRequestDTO request);
}
