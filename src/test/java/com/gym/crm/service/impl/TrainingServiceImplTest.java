package com.gym.crm.service.impl;

import com.gym.crm.facade.dto.TrainingResponseDTO;
import com.gym.crm.facade.dto.TrainingTypeDTO;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    private static final String TRAINEE_USERNAME = "Julia.Tomas";
    private static final String TRAINER_USERNAME = "Tom.Trainer";

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
    void create_shouldSaveAndReturnTraining() {
        Training training = buildTraining();
        when(trainingRepository.save(training)).thenReturn(training);

        Training result = service.create(training);

        assertThat(result).isEqualTo(training);
        verify(trainingRepository).save(training);
    }

    @Test
    void getTraineeTrainings_shouldReturnMappedList() {
        TraineeTrainingFilter filter = buildTraineeFilter();
        Training training = buildTraining();
        TrainingResponseDTO dto = buildTrainingResponseDTO();

        when(trainingRepository.findByTraineeCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate())).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTraineeTrainings(filter);

        assertThat(result).hasSize(1).contains(dto);
        verify(trainingRepository).findByTraineeCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate());
        verify(mapper).toDto(training);
    }

    @Test
    void getTraineeTrainings_shouldReturnEmptyList_whenNoTrainings() {
        TraineeTrainingFilter filter = buildTraineeFilter();

        when(trainingRepository.findByTraineeCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate())).thenReturn(List.of());

        assertThat(service.getTraineeTrainings(filter)).isEmpty();
    }

    @Test
    void getTrainerTrainings_shouldReturnMappedList() {
        TrainerTrainingFilter filter = buildTrainerFilter();
        Training training = buildTraining();
        TrainingResponseDTO dto = buildTrainingResponseDTO();

        when(trainingRepository.findByTrainerCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate())).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTrainerTrainings(filter);

        assertThat(result).hasSize(1).contains(dto);
        verify(trainingRepository).findByTrainerCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate());
        verify(mapper).toDto(training);
    }

    @Test
    void getTrainerTrainings_shouldReturnEmptyList_whenNoTrainings() {
        TrainerTrainingFilter filter = buildTrainerFilter();

        when(trainingRepository.findByTrainerCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate())).thenReturn(List.of());

        assertThat(service.getTrainerTrainings(filter)).isEmpty();
    }

    @Test
    void getAllTrainingTypes_shouldReturnMappedList() {
        TrainingType type = TrainingType.builder().trainingTypeName("Cardio").build();
        TrainingTypeDTO dto = TrainingTypeDTO.builder().trainingTypeName("Cardio").build();

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type));
        when(mapper.toDto(type)).thenReturn(dto);

        List<TrainingTypeDTO> result = service.getAllTrainingTypes();

        assertThat(result).hasSize(1).contains(dto);
        verify(trainingTypeRepository).findAll();
        verify(mapper).toDto(type);
    }

    @Test
    void getAllTrainingTypes_shouldReturnEmptyList_whenNoTypes() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllTrainingTypes()).isEmpty();
    }

    private Training buildTraining() {
        return Training.builder().trainingName("Morning Cardio").trainingDate(LocalDate.of(2024, 3, 10)).trainingDuration(60).build();
    }

    private TrainingResponseDTO buildTrainingResponseDTO() {
        return TrainingResponseDTO.builder().trainingName("Morning Cardio").trainingDate(LocalDate.of(2024, 3, 10)).trainingTypeName("Cardio")
                .trainingDuration(60).build();
    }

    private TraineeTrainingFilter buildTraineeFilter() {
        return TraineeTrainingFilter.builder().username(TRAINEE_USERNAME).fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31)).build();
    }

    private TrainerTrainingFilter buildTrainerFilter() {
        return TrainerTrainingFilter.builder().username(TRAINER_USERNAME).fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31)).build();
    }
}