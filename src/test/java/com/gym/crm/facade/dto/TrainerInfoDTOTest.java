package com.gym.crm.facade.dto;

import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerInfoDTOTest {
    @Test
    void builder_createsDtoCorrectly() {
        TrainerInfoDTO dto = TrainerInfoDTO.builder().username("tom").firstName("Tom").lastName("Tomas").isActive(true).specialization("Yoga").build();

        assertThat(dto.getUsername()).isEqualTo("tom");
        assertThat(dto.getFirstName()).isEqualTo("Tom");
        assertThat(dto.getLastName()).isEqualTo("Tomas");
        assertThat(dto.getIsActive()).isTrue();
        assertThat(dto.getSpecialization()).isEqualTo("Yoga");
    }

    @Test
    void constructor_withTrainingType_mapsSpecializationCorrectly() {
        TrainingType type = TrainingType.builder().trainingTypeName("Boxing").build();
        TrainerInfoDTO dto = new TrainerInfoDTO("tom", "Tom", "Tomas", type);

        assertThat(dto.getUsername()).isEqualTo("tom");
        assertThat(dto.getFirstName()).isEqualTo("Tom");
        assertThat(dto.getLastName()).isEqualTo("Tomas");
        assertThat(dto.getSpecialization()).isEqualTo("Boxing");
        assertThat(dto.getIsActive()).isNull();
    }

    @Test
    void equals_andHashCode_workCorrectly() {
        TrainerInfoDTO dto1 = TrainerInfoDTO.builder().username("tom").firstName("Tom").lastName("Tomas").isActive(true).specialization("Yoga").build();
        TrainerInfoDTO dto2 = TrainerInfoDTO.builder().username("tom").firstName("Tom").lastName("Tomas").isActive(true).specialization("Yoga").build();

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toString_containsAllFields() {
        TrainerInfoDTO dto = TrainerInfoDTO.builder().username("tom").firstName("Tom").lastName("Tomas").isActive(true).specialization("Yoga").build();

        String result = dto.toString();

        assertThat(result).contains("tom").contains("Tom").contains("Tomas").contains("Yoga");
    }

    @Test
    void constructor_setsIsActiveToNull() {
        TrainingType type = TrainingType.builder().trainingTypeName("Pilates").build();
        TrainerInfoDTO dto = new TrainerInfoDTO("tom", "Tom", "Tomas", type);

        assertThat(dto.getIsActive()).isNull();
    }
}