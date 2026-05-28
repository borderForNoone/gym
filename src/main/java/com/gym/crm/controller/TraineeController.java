package com.gym.crm.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import lombok.RequiredArgsConstructor;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeCreateRequest;
import org.gym.crm.rest.TraineeCreateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/trainees")
@RequiredArgsConstructor
public class TraineeController {
    private final GymFacade facade;

    @PostMapping("/register")
    public ResponseEntity<TraineeCreateResponse> register(@RequestBody TraineeCreateRequest request) {
        TraineeCreateResponse response = facade.createTrainee(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeGetResponse> getTraineeProfile(@PathVariable(name = "username") String username) {
        TraineeGetResponse response = facade.getTraineeByUsername(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(@PathVariable(name = "username") String username,
                                                                      @RequestBody TraineeUpdateRequest request) {
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
                                             @RequestBody ActivationStatusRequest request) {
        facade.toggleActiveStatus(request, username);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeAssignedTrainersUpdateResponse> updateTraineeTrainers(@PathVariable(name = "username") String username,
                                                                                       @RequestBody TraineeAssignedTrainersUpdateRequest request) {
        TraineeAssignedTrainersUpdateResponse response = facade.updateTraineeTrainersList(request, username);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/available-trainers")
    public ResponseEntity<List<AssignedTrainerResponse>> getAvailableTrainers(@PathVariable(name = "username") String username) {
        List<AssignedTrainerResponse> response = facade.getTrainersNotAssignedToTrainee(username);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<GetTraineeTrainingResponse>> getTraineeTrainings(@PathVariable(name = "username") String username,
                                                                                @RequestParam(name = "fromDate", required = false)
                                                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                                @RequestParam(name = "toDate", required = false)
                                                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                                @RequestParam(name = "trainerName", required = false) String trainerName,
                                                                                @RequestParam(name = "trainingType", required = false) String trainingType) {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder()
                .username(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .joinFullName(trainerName)
                .trainingTypeName(trainingType)
                .build();
        List<GetTraineeTrainingResponse> response = facade.getTraineeTrainingsByFilter(filter);

        return ResponseEntity.ok(response);
    }
}
