package org.gym.crm.controller;

import org.gym.crm.facade.GymFacade;
import lombok.RequiredArgsConstructor;
import org.gym.crm.rest.LoginChangeRequest;
import org.gym.crm.rest.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GymFacade facade;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        facade.login(request);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody LoginChangeRequest request) {
        facade.changePassword(request, request.getUsername());

        return ResponseEntity.ok().build();
    }
}
