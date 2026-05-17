package org.gym.crm.mapper;

import org.gym.crm.dto.trainer.TrainerRequestDTO;
import org.gym.crm.dto.trainer.TrainerResponseDTO;
import org.gym.crm.dto.trainer.TrainerUpdateDTO;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TrainerMapper {
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.isActive", constant = "true")
    @Mapping(target = "specialization", source = "specialization")
    Trainer toEntity(TrainerRequestDTO dto);

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.isActive", source = "isActive")
    @Mapping(target = "specialization", source = "specialization")
    Trainer toEntity(TrainerUpdateDTO dto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive", qualifiedByName = "booleanDefault")
    @Mapping(source = "specialization", target = "specialization")
    TrainerResponseDTO toDto(Trainer trainer);

    @Named("booleanDefault")
    default Boolean booleanDefault(Boolean value) {
        return value != null && value;
    }

    default TrainingType map(String type) {
        return type == null ? null : TrainingType.builder()
                .trainingTypeName(type)
                .build();
    }

    default String map(TrainingType type) {
        return type == null ? null : type.getTrainingTypeName();
    }
}
