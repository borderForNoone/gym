package com.gym.crm.mapper;

import com.gym.crm.dto.TraineeInfoDTO;
import com.gym.crm.dto.TraineeRequestDTO;
import com.gym.crm.dto.TraineeResponseDTO;
import com.gym.crm.dto.TraineeUpdateDTO;
import com.gym.crm.model.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeMapper {
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.isActive", constant = "true")
    Trainee toEntity(TraineeRequestDTO traineeRequestDTO);

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.isActive", source = "isActive")
    Trainee toEntity(TraineeUpdateDTO traineeUpdateDTO);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "user.password", target = "password")
    TraineeResponseDTO toDto(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    TraineeInfoDTO toInfoDto(Trainee trainee);
}
