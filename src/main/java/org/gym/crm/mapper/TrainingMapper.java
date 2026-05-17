package org.gym.crm.mapper;

import org.gym.crm.dto.training.TrainingRequestDTO;
import org.gym.crm.dto.training.TrainingResponseDTO;
import org.gym.crm.model.Training;
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
}
