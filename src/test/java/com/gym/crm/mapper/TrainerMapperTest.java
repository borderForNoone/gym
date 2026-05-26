package com.gym.crm.mapper;

import com.gym.crm.facade.dto.TrainerRequestDTO;
import com.gym.crm.facade.dto.TrainerResponseDTO;
import com.gym.crm.facade.dto.TrainerUpdateDTO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.gym.crm.util.TestConstants.FIRST_NAME;
import static com.gym.crm.util.TestConstants.FITNESS;
import static com.gym.crm.util.TestConstants.ID;
import static com.gym.crm.util.TestConstants.LAST_NAME;
import static com.gym.crm.util.TestConstants.TRAINER_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerMapperTest {
    private TrainerMapper trainerMapper;
    private TrainingType fitness;

    @BeforeEach
    void setUp() {
        trainerMapper = Mappers.getMapper(TrainerMapper.class);
        fitness = buildFitnessType();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        TrainerRequestDTO request = TrainerRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).specialization(fitness.getTrainingTypeName()).build();

        Trainer actual = trainerMapper.toEntity(request);

        assertNotNull(actual);
        assertNotNull(actual.getUser());
        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertNull(actual.getUser().getUsername());
        assertNull(actual.getUser().getPassword());
        assertTrue(actual.getUser().getIsActive());

        assertNotNull(actual.getSpecialization());
        assertEquals(fitness.getTrainingTypeName(), actual.getSpecialization().getTrainingTypeName());
    }

    @Test
    void toEntity_shouldNotSetUsernameAndPassword() {
        TrainerRequestDTO request = TrainerRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).specialization(fitness.getTrainingTypeName()).build();

        Trainer actual = trainerMapper.toEntity(request);

        assertNull(actual.getUser().getUsername());
        assertNull(actual.getUser().getPassword());
    }

    @Test
    void toResponseDto_shouldMapAllFields() {
        TrainingType specialization = TrainingType.builder().id(1L).trainingTypeName("fitness").build();
        User user = User.builder().id(ID).firstName(FIRST_NAME).lastName(LAST_NAME).username(TRAINER_USERNAME).isActive(true).build();
        Trainer trainer = Trainer.builder().id(ID).user(user).specialization(specialization).build();

        TrainerResponseDTO actual = trainerMapper.toDto(trainer);

        assertEquals(ID, actual.getUserId());
        assertEquals(TRAINER_USERNAME, actual.getUsername());
        assertEquals(FIRST_NAME, actual.getFirstName());
        assertEquals(LAST_NAME, actual.getLastName());
        assertTrue(actual.getIsActive());
        assertEquals("fitness", actual.getSpecialization());
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestIsNull() {
        assertNull(trainerMapper.toEntity((TrainerRequestDTO) null));
    }

    @Test
    void toEntity_update_shouldReturnNull_whenDtoIsNull() {
        assertNull(trainerMapper.toEntity((TrainerUpdateDTO) null));
    }

    @Test
    void toDto_shouldReturnNull_whenTrainerIsNull() {
        assertNull(trainerMapper.toDto(null));
    }

    @Test
    void toEntity_update_shouldMapAllFields() {
        TrainerUpdateDTO dto = TrainerUpdateDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).isActive(true).specialization(FITNESS).build();

        Trainer actual = trainerMapper.toEntity(dto);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertTrue(actual.getUser().getIsActive());
        assertEquals(FITNESS, actual.getSpecialization().getTrainingTypeName());
    }

    @Test
    void toDto_shouldHandleNullUser() {
        Trainer trainer = Trainer.builder().user(null).specialization(fitness).build();

        TrainerResponseDTO actual = trainerMapper.toDto(trainer);

        assertNull(actual.getUsername());
        assertNull(actual.getFirstName());
        assertNull(actual.getLastName());
        assertFalse(actual.getIsActive());
    }

    @Test
    void toDto_shouldHandleNullSpecialization() {
        User user = User.builder().firstName(FIRST_NAME).lastName(LAST_NAME).username(TRAINER_USERNAME).isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).specialization(null).build();

        TrainerResponseDTO actual = trainerMapper.toDto(trainer);

        assertNull(actual.getSpecialization());
    }

    @Test
    void map_shouldReturnNull_whenStringIsNull() {
        assertNull(trainerMapper.map((String) null));
    }

    @Test
    void map_shouldConvertStringToTrainingType() {
        TrainingType actual = trainerMapper.map(FITNESS);

        assertEquals(FITNESS, actual.getTrainingTypeName());
    }

    @Test
    void map_shouldReturnNull_whenTrainingTypeIsNull() {
        assertNull(trainerMapper.map((TrainingType) null));
    }

    private TrainingType buildFitnessType() {
        return TrainingType.builder().trainingTypeName(FITNESS).build();
    }
}