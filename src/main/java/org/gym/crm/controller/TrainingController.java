package org.gym.crm.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.TrainingTypeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}")
@RequiredArgsConstructor
public class TrainingController {
    private final GymFacade facade;

    @PostMapping("/trainings")
    public ResponseEntity<Void> addTraining(@RequestBody @Valid TrainingRequestDTO request) {
        facade.createTraining(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainings/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(facade.getTrainingTypes());
    }
}
