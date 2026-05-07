package org.gym.crm.service.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.dao.TrainingDao;
import org.gym.crm.model.Training;
import org.gym.crm.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {
    @Autowired
    @Setter
    private TrainingDao trainingDao;

    @Transactional
    @Override
    public Training create(Training training) {
        log.info("Creating training: {}", training.getTrainingName());
        return trainingDao.save(training);
    }
}
