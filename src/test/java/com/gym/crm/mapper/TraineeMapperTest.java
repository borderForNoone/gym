package com.gym.crm.mapper;

import com.gym.crm.dto.TraineeRequestDTO;
import com.gym.crm.dto.TraineeResponseDTO;
import com.gym.crm.dto.TraineeUpdateDTO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.gym.crm.util.TestConstants.ADDRESS;
import static com.gym.crm.util.TestConstants.DATE_OF_BIRTH;
import static com.gym.crm.util.TestConstants.FIRST_NAME;
import static com.gym.crm.util.TestConstants.ID;
import static com.gym.crm.util.TestConstants.LAST_NAME;
import static com.gym.crm.util.TestConstants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeMapperTest {
    private TraineeMapper traineeMapper;

    @BeforeEach
    void setUp() {
        traineeMapper = Mappers.getMapper(TraineeMapper.class);
    }

    @Test
    void toEntity_shouldMapAllFields() {
        TraineeRequestDTO request = TraineeRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).dateOfBirth(DATE_OF_BIRTH).address(ADDRESS).build();

        Trainee actual = traineeMapper.toEntity(request);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertTrue(actual.getUser().getIsActive());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());
    }

    @Test
    void toEntity_shouldNotSetUsernameAndPassword() {
        TraineeRequestDTO request = TraineeRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).dateOfBirth(DATE_OF_BIRTH).address(ADDRESS).build();

        Trainee actual = traineeMapper.toEntity(request);

        assertNull(actual.getUser().getUsername());
        assertNull(actual.getUser().getPassword());
    }

    @Test
    void toResponseDto_shouldMapAllFields() {
        Trainee trainee = Trainee.builder().id(ID).user(User.builder().id(ID).firstName(FIRST_NAME).lastName(LAST_NAME).username(USERNAME).isActive(true).build())
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();

        TraineeResponseDTO actual = traineeMapper.toDto(trainee);

        assertEquals(ID, actual.getUserId());
        assertEquals(USERNAME, actual.getUsername());
        assertEquals(FIRST_NAME, actual.getFirstName());
        assertEquals(LAST_NAME, actual.getLastName());
        assertTrue(actual.getIsActive());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());
    }

    @Test
    void toResponseDto_shouldHandleNullDateOfBirth() {
        User user = User.builder().firstName(FIRST_NAME).lastName(LAST_NAME).username(USERNAME).isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).dateOfBirth(null).build();

        TraineeResponseDTO actual = traineeMapper.toDto(trainee);

        assertNull(actual.getDateOfBirth());
    }

    @Test
    void toEntity_shouldReturnNull_whenDtoIsNull() {
        assertNull(traineeMapper.toEntity((TraineeRequestDTO) null));
    }

    @Test
    void toEntity_shouldMapActiveUser_byDefault() {
        TraineeRequestDTO request = TraineeRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).build();

        Trainee actual = traineeMapper.toEntity(request);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertTrue(actual.getUser().getIsActive());
    }

    @Test
    void toEntity_updateDto_shouldReturnNull_whenDtoIsNull() {
        assertNull(traineeMapper.toEntity((TraineeUpdateDTO) null));
    }

    @Test
    void toEntity_updateDto_shouldMapFields() {
        TraineeUpdateDTO dto = TraineeUpdateDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).isActive(true).address(ADDRESS).dateOfBirth(DATE_OF_BIRTH).build();

        Trainee actual = traineeMapper.toEntity(dto);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertTrue(actual.getUser().getIsActive());
        assertEquals(ADDRESS, actual.getAddress());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
    }

    @Test
    void toDto_shouldReturnNull_whenTraineeIsNull() {
        assertNull(traineeMapper.toDto(null));
    }

    @Test
    void toDto_shouldHandleNullUser() {
        Trainee trainee = Trainee.builder().user(null).dateOfBirth(DATE_OF_BIRTH).address(ADDRESS).build();

        TraineeResponseDTO actual = traineeMapper.toDto(trainee);

        assertNull(actual.getUsername());
        assertNull(actual.getFirstName());
        assertNull(actual.getLastName());
        assertNull(actual.getUserId());
        assertNull(actual.getIsActive());
    }
}