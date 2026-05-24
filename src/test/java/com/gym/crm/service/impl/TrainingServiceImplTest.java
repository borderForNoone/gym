package com.gym.crm.service.impl;

import com.gym.crm.config.TransactionManager;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.dto.TrainingResponseDTO;
import com.gym.crm.dto.TrainingTypeDTO;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.common.UserInputValidator;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private TrainingDao dao;
    @Mock
    private UserInputValidator validator;
    @Mock
    private TrainingMapper mapper;
    @Mock
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private Session session;

    private TrainingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TrainingServiceImpl(dao, validator, mapper, trainingTypeDao, transactionManager);
    }

    @Test
    void create_shouldSaveTraining() {
        Training training = Training.builder().trainingName("Yoga").build();
        Training saved = Training.builder().trainingName("Yoga").build();

        when(transactionManager.performReturningWithinTx(any())).thenAnswer(inv -> {
            Function<Session, Object> fn = inv.getArgument(0);
            return fn.apply(session);
        });
        when(dao.save(training)).thenReturn(saved);

        Training result = service.create(training);

        assertEquals(saved, result);
        verify(dao).save(training);
    }

    @Test
    void getTraineeTrainings_shouldReturnMappedList() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();
        Training training = Training.builder().trainingName("Yoga").build();
        TrainingResponseDTO dto = TrainingResponseDTO.builder().trainingName("Yoga").build();

        when(dao.findByTraineeCriteria(filter)).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTraineeTrainings(filter);

        assertEquals(1, result.size());
        assertEquals(dto, result.getFirst());
        verify(validator).validate(filter, "Filter");
        verify(dao).findByTraineeCriteria(filter);
    }

    @Test
    void getTrainerTrainings_shouldReturnMappedList() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();
        Training training = Training.builder().trainingName("Boxing").build();
        TrainingResponseDTO dto = TrainingResponseDTO.builder().trainingName("Boxing").build();

        when(dao.findByTrainerCriteria(filter)).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTrainerTrainings(filter);

        assertEquals(1, result.size());
        assertEquals(dto, result.getFirst());
        verify(validator).validate(filter, "Filter");
        verify(dao).findByTrainerCriteria(filter);
    }

    @Test
    void getAllTrainingTypes_shouldReturnMappedList() {
        var type = TrainingType.builder().trainingTypeName("Yoga").build();
        TrainingTypeDTO dto = TrainingTypeDTO.builder().trainingTypeName("Yoga").build();

        when(trainingTypeDao.findAll()).thenReturn(List.of(type));
        when(mapper.toDto(type)).thenReturn(dto);

        List<TrainingTypeDTO> result = service.getAllTrainingTypes();

        assertEquals(1, result.size());
        assertEquals(dto, result.getFirst());

        verify(trainingTypeDao).findAll();
    }
}