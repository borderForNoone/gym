package org.gym.crm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.rest.TrainerUpdateRequest;
import org.gym.crm.rest.TrainerUpdateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/trainers")
@RequiredArgsConstructor
public class TrainerController {
    private final GymFacade facade;

    @PostMapping("/register")
    public ResponseEntity<TrainerCreateResponse> register(@RequestBody @Valid TrainerCreateRequest request) {
        TrainerCreateResponse response = facade.createTrainer(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerGetResponse> getTrainerProfile(@PathVariable(name = "username") String username) {
        TrainerGetResponse response = facade.getTrainerByUsername(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerUpdateResponse> updateTrainerProfile(@PathVariable(name = "username") String username,
                                                                      @RequestBody @Valid TrainerUpdateRequest request) {
        TrainerUpdateResponse response = facade.updateTrainer(request, username);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/activation")
    public ResponseEntity<Void> toggleActive(@PathVariable(name = "username") String username,
                                             @RequestBody @Valid ActivationStatusRequest request) {
        facade.toggleActiveStatus(request, username);

        return ResponseEntity.ok().build();
    }
}
