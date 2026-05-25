package com.gym.crm.service.impl;

import com.gym.crm.dto.TrainingResponseDTO;
import com.gym.crm.dto.TrainingTypeDTO;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Training;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.common.UserInputValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private UserInputValidator validator;
    @Mock
    private TrainingMapper mapper;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void create_shouldSaveTraining() {
        Training training = mock(Training.class);

        when(trainingRepository.save(training)).thenReturn(training);

        Training result = service.create(training);

        assertThat(result).isNotNull();
        verify(trainingRepository).save(training);
    }

    @Test
    void getTraineeTrainings_shouldReturnMappedList() {
        TraineeTrainingFilter filter = mock(TraineeTrainingFilter.class);

        when(filter.getUsername()).thenReturn("user");
        when(filter.getFromDate()).thenReturn(null);
        when(filter.getToDate()).thenReturn(null);

        Training training = mock(Training.class);
        TrainingResponseDTO dto = mock(TrainingResponseDTO.class);

        when(trainingRepository.findByTraineeCriteria("user", null, null)).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTraineeTrainings(filter);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto);

        verify(validator).validate(filter, "Filter");
        verify(trainingRepository).findByTraineeCriteria("user", null, null);
    }

    @Test
    void getTrainerTrainings_shouldReturnMappedList() {
        TrainerTrainingFilter filter = mock(TrainerTrainingFilter.class);

        when(filter.getUsername()).thenReturn("trainer");
        when(filter.getFromDate()).thenReturn(null);
        when(filter.getToDate()).thenReturn(null);

        Training training = mock(Training.class);
        TrainingResponseDTO dto = mock(TrainingResponseDTO.class);

        when(trainingRepository.findByTrainerCriteria("trainer", null, null)).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTrainerTrainings(filter);

        assertThat(result).hasSize(1);

        verify(validator).validate(filter, "Filter");
    }

    @Test
    void getAllTrainingTypes_shouldReturnList() {
        TrainingTypeDTO dto = mock(TrainingTypeDTO.class);
        var type = mock(com.gym.crm.model.TrainingType.class);

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type));
        when(mapper.toDto(type)).thenReturn(dto);

        List<TrainingTypeDTO> result = service.getAllTrainingTypes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);

        verify(trainingTypeRepository).findAll();
    }
}