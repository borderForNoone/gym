package org.gym.crm.mapper;

import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainerRestMapper {
    AssignedTrainerResponse toRest(TrainerInfoDTO dto);
}
