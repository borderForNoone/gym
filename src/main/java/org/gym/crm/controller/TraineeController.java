package org.gym.crm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
public class TraineeController {
    private final GymFacade facade;

    @PostMapping("/register")
    public ResponseEntity<TraineeResponseDTO> register(@RequestBody @Valid TraineeRequestDTO request) {
        TraineeResponseDTO response = facade.createTrainee(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeGetResponse> getTraineeProfile(@PathVariable(name = "username") String username) {
        TraineeGetResponse response = facade.getTraineeByUsername(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(@PathVariable(name = "username") String username,
                                                                      @RequestBody @Valid TraineeUpdateRequest request) {
        TraineeUpdateResponse response = facade.updateTrainee(request, username);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTrainee(@PathVariable(name = "username") String username) {
        facade.deleteTraineeByUsername(username);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/activation")
    public ResponseEntity<Void> toggleActive(@PathVariable(name = "username") String username,
                                             @RequestBody @Valid ActivationStatusRequest request) {
        facade.toggleActiveStatus(request, username);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeAssignedTrainersUpdateResponse> updateTraineeTrainers(@PathVariable(name = "username") String username,
                                                                                       @RequestBody @Valid TraineeAssignedTrainersUpdateRequest request) {
        TraineeAssignedTrainersUpdateResponse response = facade.updateTraineeTrainersList(request, username);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/available-trainers")
    public ResponseEntity<List<AssignedTrainerResponse>> getAvailableTrainers(@PathVariable(name = "username") String username) {
        List<AssignedTrainerResponse> response = facade.getTrainersNotAssignedToTrainee(username);

        return ResponseEntity.ok(response);
    }
}
