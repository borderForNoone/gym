package org.gym.crm.mapper;

import org.gym.crm.dto.TraineeInfoDTO;
import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.dto.TraineeUpdateDTO;
import org.gym.crm.rest.TraineeCreateRequest;
import org.gym.crm.rest.TraineeCreateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeRestMapper {

    TraineeRequestDTO toDto(TraineeCreateRequest request);

    TraineeCreateResponse toRest(TraineeResponseDTO dto);

    TraineeGetResponse toRest(TraineeInfoDTO dto);

    @Mapping(target = "username", source = "username")
    TraineeUpdateDTO toDto(String username, TraineeUpdateRequest request);

    TraineeUpdateResponse toRestUpdateResponse(TraineeResponseDTO dto);
}
