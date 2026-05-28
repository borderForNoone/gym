package com.gym.crm.service.impl;

import com.gym.crm.facade.dto.TrainingResponseDTO;
import com.gym.crm.facade.dto.TrainingTypeDTO;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Training;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.common.UserInputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private final UserInputValidator validator;
    private final TrainingMapper mapper;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    @Transactional
    @Override
    public Training create(Training training) {
        log.info("Creating training: {}", training.getTrainingName());

        return trainingRepository.save(training);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrainingResponseDTO> getTraineeTrainings(@Valid TraineeTrainingFilter filter) {
        validator.validate(filter, "Filter");
        log.info("Getting trainee trainings by filter: {}", filter);

        return trainingRepository.findByTraineeCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate()).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrainingResponseDTO> getTrainerTrainings(@Valid TrainerTrainingFilter filter) {
        validator.validate(filter, "Filter");
        log.info("Getting trainer trainings by filter: {}", filter);

        return trainingRepository.findByTrainerCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate()).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrainingTypeDTO> getAllTrainingTypes() {
        return trainingTypeRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }
}