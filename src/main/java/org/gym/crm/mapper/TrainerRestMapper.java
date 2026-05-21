package org.gym.crm.mapper;

import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainerUpdateDTO;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.rest.TrainerUpdateRequest;
import org.gym.crm.rest.TrainerUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerRestMapper {
    AssignedTrainerResponse toRest(TrainerInfoDTO dto);

    TrainerCreateResponse toRest(TrainerResponseDTO dto);

    TrainerGetResponse toRestGetResponse(TrainerInfoDTO dto);

    @Mapping(target = "username", source = "username")
    TrainerUpdateDTO toDto(String username, TrainerUpdateRequest request);

    TrainerRequestDTO toDto(TrainerCreateRequest request);

    TrainerUpdateResponse toRestUpdateResponse(TrainerResponseDTO dto);
}
