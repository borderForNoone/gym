package org.gym.crm.mapper;

import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.dto.TrainingTypeDTO;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.GetTrainerTrainingResponse;
import org.gym.crm.rest.TrainingCreateRequest;
import org.gym.crm.rest.TrainingTypeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingRestMapper {
    TrainingRequestDTO toDto(TrainingCreateRequest request);

    TrainingTypeResponse toRest(TrainingTypeDTO dto);

    GetTraineeTrainingResponse toRestTraineeResponse(TrainingResponseDTO dto);

    GetTrainerTrainingResponse toRestTrainerResponse(TrainingResponseDTO dto);
}
