package com.gym.crm.mapper;

import com.gym.crm.dto.TrainingRequestDTO;
import com.gym.crm.dto.TrainingResponseDTO;
import com.gym.crm.dto.TrainingTypeDTO;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    @Mapping(target = "trainingType.trainingTypeName", source = "trainingTypeName")
    Training toEntity(TrainingRequestDTO trainingRequestDTO);

    @Mapping(source = "trainee.id", target = "traineeId")
    @Mapping(source = "trainer.id", target = "trainerId")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingTypeName")
    TrainingResponseDTO toDto(Training training);

    TrainingTypeDTO toDto(TrainingType trainingType);
}
