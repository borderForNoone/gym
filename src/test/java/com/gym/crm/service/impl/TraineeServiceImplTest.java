package com.gym.crm.service.impl;

import com.gym.crm.dto.CreatedTrainee;
import com.gym.crm.dto.TraineeInfoDTO;
import com.gym.crm.dto.TraineeResponseDTO;
import com.gym.crm.dto.TraineeUpdateDTO;
import com.gym.crm.dto.TrainerAssignmentUpdateDTO;
import com.gym.crm.dto.TrainerInfoDTO;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.UserInputValidator;
import com.gym.crm.util.CoreValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserProfileService userCredentialGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TraineeMapper mapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private CoreValidator validator;
    @Mock
    private UserInputValidator userInputValidator;
    @InjectMocks
    private TraineeServiceImpl service;

    @Test
    void create_shouldReturnCreatedTrainee() {
        User user = User.builder().firstName("John").lastName("Doe").build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(userCredentialGenerator.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(userCredentialGenerator.generatePassword()).thenReturn("rawPass");
        when(passwordEncoder.encode("rawPass")).thenReturn("encoded");
        when(traineeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatedTrainee result = service.create(trainee);

        assertThat(result).isNotNull();
        verify(validator).validateTrainee(any());
        verify(traineeRepository).save(any());
    }

    @Test
    void update_shouldReturnDto() {
        TraineeUpdateDTO dto = mock(TraineeUpdateDTO.class);

        when(dto.getUsername()).thenReturn("user1");
        when(dto.getFirstName()).thenReturn("New");
        when(dto.getLastName()).thenReturn("Name");
        when(dto.getIsActive()).thenReturn(true);

        User user = User.builder().username("user1").firstName("Old").lastName("OldName").isActive(false).build();
        Trainee trainee = Trainee.builder().user(user).build();

        TraineeResponseDTO responseDTO = mock(TraineeResponseDTO.class);

        when(traineeRepository.findByUser_Username("user1")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any())).thenReturn(trainee);
        when(mapper.toDto(any())).thenReturn(responseDTO);

        TraineeResponseDTO result = service.update(dto);

        assertThat(result).isNotNull();
        verify(userInputValidator).validate(dto, "Trainee");
        verify(traineeRepository).save(any());
    }

    @Test
    void getTraineeByUsername_shouldReturnDto() {
        Trainee trainee = mock(Trainee.class);
        TraineeInfoDTO dto = mock(TraineeInfoDTO.class);

        doNothing().when(userInputValidator).validateUsername("user");
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(mapper.toInfoDto(trainee)).thenReturn(dto);

        TraineeInfoDTO result = service.getTraineeByUsername("user");

        assertThat(result).isNotNull();
        verify(userInputValidator).validateUsername("user");
        verify(traineeRepository).findByUser_Username("user");
    }

    @Test
    void changePassword_shouldChangePassword() {
        User user = User.builder().username("user").password("old").isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("oldPass", "old")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        service.changePassword("user", "oldPass", "newPass");

        verify(passwordEncoder).encode("newPass");
        verify(traineeRepository).save(any());
    }

    @Test
    void changePassword_shouldThrow_whenWrongPassword() {
        Trainee trainee = mock(Trainee.class, RETURNS_DEEP_STUBS);
        User user = mock(User.class);

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(trainee.getUser()).thenReturn(user);
        when(user.getPassword()).thenReturn("hashed");
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword("user", "bad", "new")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteByUsername_shouldDelete() {
        Trainee trainee = mock(Trainee.class);

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));

        service.deleteByUsername("user");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void getUnassignedTrainers_shouldReturnFilteredList() {
        Trainee trainee = mock(Trainee.class);
        Trainer trainer1 = mock(Trainer.class);
        Trainer trainer2 = mock(Trainer.class);

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAll()).thenReturn(List.of(trainer1, trainer2));
        when(trainee.getTrainers()).thenReturn(Set.of(trainer1));

        List<Trainer> result = service.getUnassignedTrainers("user");

        assertThat(result).containsExactly(trainer2);
    }

    @Test
    void updateTrainersList_shouldReplaceList() {
        TrainerAssignmentUpdateDTO dto = mock(TrainerAssignmentUpdateDTO.class);

        when(dto.getTraineeUsername()).thenReturn("user");
        when(dto.getTrainerUsernames()).thenReturn(List.of("t1"));

        Trainee trainee = mock(Trainee.class);
        Trainer trainer = mock(Trainer.class);

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("t1")).thenReturn(Optional.of(trainer));

        Set<Trainer> trainers = new HashSet<>();
        trainers.add(trainer);
        when(trainee.getTrainers()).thenReturn(trainers);

        when(trainer.getId()).thenReturn(1L);
        when(trainerRepository.findAllById(List.of(1L))).thenReturn(List.of(trainer));
        when(trainerMapper.toInfoDto(trainer)).thenReturn(mock(TrainerInfoDTO.class));

        List<TrainerInfoDTO> result = service.updateTrainersList(dto);

        assertThat(result).hasSize(1);
        verify(traineeRepository).findByUser_Username("user");
    }

    @Test
    void updateProfile_shouldUpdateFields() {
        User existingUser = User.builder().username("user").firstName("Old").lastName("Name").isActive(true).build();
        Trainee trainee = Trainee.builder().user(existingUser).build();
        User incomingUser = User.builder().firstName("New").lastName("Name").isActive(true).build();
        Trainee updated = Trainee.builder().user(incomingUser).dateOfBirth(null).address(null).build();

        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any())).thenReturn(updated);

        Trainee result = service.updateProfile("user", updated);

        assertThat(result).isNotNull();
        verify(traineeRepository).findByUser_Username("user");
        verify(traineeRepository).save(any());
    }
}