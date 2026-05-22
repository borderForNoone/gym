package org.gym.crm.service.impl;

import org.gym.crm.dao.TrainingDao;
import org.gym.crm.dao.TrainingTypeDao;
import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.dto.TrainingTypeDTO;
import org.gym.crm.mapper.TrainingMapper;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.service.common.UserInputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private TrainingDao dao;
    @Mock
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private TrainingMapper mapper;
    @Mock
    private UserInputValidator validator;

    @InjectMocks
    private TrainingServiceImpl service;

    private Training training;
    private TraineeTrainingFilter traineeFilter;
    private TrainerTrainingFilter trainerFilter;

    @BeforeEach
    void setUp() {
        training = Training.builder()
                .trainingName("Morning Workout")
                .build();

        traineeFilter = TraineeTrainingFilter.builder()
                .trainingTypeName("Yoga")
                .build();

        trainerFilter = TrainerTrainingFilter.builder()
                .build();
    }

    @Test
    void create_shouldSaveAndReturnTraining() {
        when(dao.save(training)).thenReturn(training);

        Training result = service.create(training);

        assertThat(result).isEqualTo(training);
        verify(dao).save(training);
    }

    @Test
    void create_shouldPropagateExceptionFromDao() {
        when(dao.save(training)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> service.create(training))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    void getTraineeTrainings_shouldValidateFilterBeforeQuerying() {
        when(dao.findByTraineeCriteria(traineeFilter)).thenReturn(List.of());

        service.getTraineeTrainings(traineeFilter);

        var inOrder = inOrder(validator, dao);
        inOrder.verify(validator).validate(traineeFilter, "Filter");
        inOrder.verify(dao).findByTraineeCriteria(traineeFilter);
    }

    @Test
    void getTraineeTrainings_shouldReturnMappedDtos() {
        Training t1 = Training.builder().trainingName("A").build();
        Training t2 = Training.builder().trainingName("B").build();
        TrainingResponseDTO dto1 = TrainingResponseDTO.builder().build(); // adjust if needed
        TrainingResponseDTO dto2 = TrainingResponseDTO.builder().build();

        when(dao.findByTraineeCriteria(traineeFilter)).thenReturn(List.of(t1, t2));
        when(mapper.toDto(t1)).thenReturn(dto1);
        when(mapper.toDto(t2)).thenReturn(dto2);

        List<TrainingResponseDTO> result = service.getTraineeTrainings(traineeFilter);

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    void getTraineeTrainings_shouldReturnEmptyListWhenNoneFound() {
        when(dao.findByTraineeCriteria(traineeFilter)).thenReturn(List.of());

        List<TrainingResponseDTO> result = service.getTraineeTrainings(traineeFilter);

        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }

    @Test
    void getTrainerTrainings_shouldValidateFilterBeforeQuerying() {
        when(dao.findByTrainerCriteria(trainerFilter)).thenReturn(List.of());

        service.getTrainerTrainings(trainerFilter);

        var inOrder = inOrder(validator, dao);
        inOrder.verify(validator).validate(trainerFilter, "Filter");
        inOrder.verify(dao).findByTrainerCriteria(trainerFilter);
    }

    @Test
    void getTrainerTrainings_shouldReturnMappedDtos() {
        Training t1 = Training.builder().trainingName("C").build();
        TrainingResponseDTO dto1 = TrainingResponseDTO.builder().build();

        when(dao.findByTrainerCriteria(trainerFilter)).thenReturn(List.of(t1));
        when(mapper.toDto(t1)).thenReturn(dto1);

        List<TrainingResponseDTO> result = service.getTrainerTrainings(trainerFilter);

        assertThat(result).containsExactly(dto1);
    }

    @Test
    void getTrainerTrainings_shouldReturnEmptyListWhenNoneFound() {
        when(dao.findByTrainerCriteria(trainerFilter)).thenReturn(List.of());

        List<TrainingResponseDTO> result = service.getTrainerTrainings(trainerFilter);

        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }

    @Test
    void getAllTrainingTypes_shouldReturnMappedDtos() {
        TrainingType type1 = TrainingType.builder().id(1L).trainingTypeName("Yoga").build();
        TrainingType type2 = TrainingType.builder().id(2L).trainingTypeName("Boxing").build();
        TrainingTypeDTO dto1 = TrainingTypeDTO.builder().id(1L).trainingTypeName("Yoga").build();
        TrainingTypeDTO dto2 = TrainingTypeDTO.builder().id(2L).trainingTypeName("Boxing").build();

        when(trainingTypeDao.findAll()).thenReturn(List.of(type1, type2));
        when(mapper.toDto(type1)).thenReturn(dto1);
        when(mapper.toDto(type2)).thenReturn(dto2);

        List<TrainingTypeDTO> result = service.getAllTrainingTypes();

        assertThat(result).containsExactly(dto1, dto2);
        verify(trainingTypeDao).findAll();
    }

    @Test
    void getAllTrainingTypes_shouldReturnEmptyListWhenNoneExist() {
        when(trainingTypeDao.findAll()).thenReturn(List.of());

        List<TrainingTypeDTO> result = service.getAllTrainingTypes();

        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }
}