package com.gym.crm.facade.dto;

import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerInfoDTOTest {
    @Test
    void builder_createsDtoCorrectly() {
        TrainerInfoDTO dto = TrainerInfoDTO.builder().username("john").firstName("John").lastName("Doe").isActive(true).specialization("Yoga").build();

        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getIsActive()).isTrue();
        assertThat(dto.getSpecialization()).isEqualTo("Yoga");
    }

    @Test
    void constructor_withTrainingType_mapsSpecializationCorrectly() {
        TrainingType type = TrainingType.builder().trainingTypeName("Boxing").build();
        TrainerInfoDTO dto = new TrainerInfoDTO("john", "John", "Doe", type);

        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getSpecialization()).isEqualTo("Boxing");
        assertThat(dto.getIsActive()).isNull();
    }

    @Test
    void equals_andHashCode_workCorrectly() {
        TrainerInfoDTO dto1 = TrainerInfoDTO.builder().username("john").firstName("John").lastName("Doe").isActive(true).specialization("Yoga").build();
        TrainerInfoDTO dto2 = TrainerInfoDTO.builder().username("john").firstName("John").lastName("Doe").isActive(true).specialization("Yoga").build();

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toString_containsAllFields() {
        TrainerInfoDTO dto = TrainerInfoDTO.builder().username("john").firstName("John").lastName("Doe").isActive(true).specialization("Yoga").build();

        String result = dto.toString();

        assertThat(result).contains("john").contains("John").contains("Doe").contains("Yoga");
    }

    @Test
    void constructor_setsIsActiveToNull() {
        TrainingType type = TrainingType.builder().trainingTypeName("Pilates").build();
        TrainerInfoDTO dto = new TrainerInfoDTO("john", "John", "Doe", type);

        assertThat(dto.getIsActive()).isNull();
    }
}