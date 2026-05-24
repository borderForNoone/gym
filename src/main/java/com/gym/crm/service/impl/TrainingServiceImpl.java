package com.gym.crm.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gym.crm.config.TransactionManager;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.dto.TrainingResponseDTO;
import com.gym.crm.dto.TrainingTypeDTO;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Training;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.common.UserInputValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private final TrainingDao dao;
    private final UserInputValidator validator;
    private final TrainingMapper mapper;
    private final TrainingTypeDao trainingTypeDao;
    private final TransactionManager transactionManager;

    @Override
    public Training create(Training training) {
        return transactionManager.performReturningWithinTx(session -> {
            log.info("Creating training: {}", training.getTrainingName());

            return dao.save(training);
        });
    }

    @Override
    public List<TrainingResponseDTO> getTraineeTrainings(@Valid TraineeTrainingFilter filter) {
        validator.validate(filter, "Filter");
        log.info("Getting trainee trainings by filter: {}", filter);

        return dao.findByTraineeCriteria(filter).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<TrainingResponseDTO> getTrainerTrainings(@Valid TrainerTrainingFilter filter) {
        validator.validate(filter, "Filter");
        log.info("Getting trainer trainings by filter: {}", filter);

        return dao.findByTrainerCriteria(filter).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<TrainingTypeDTO> getAllTrainingTypes() {
        return trainingTypeDao.findAll().stream().map(mapper::toDto).toList();
    }
}