package com.gym.crm.service.impl;

import com.gym.crm.exception.InvalidPasswordException;
import com.gym.crm.facade.dto.CreatedTrainer;
import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.facade.dto.TrainerRequestDTO;
import com.gym.crm.facade.dto.TrainerResponseDTO;
import com.gym.crm.facade.dto.TrainerUpdateDTO;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.CoreValidator;
import com.gym.crm.service.common.UserInputValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private CoreValidator validator;
    @Mock
    private UserInputValidator userInputValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TrainerMapper mapper;

    @InjectMocks
    private TrainerServiceImpl service;

    @Test
    void createTrainer_shouldReturnCreatedTrainer() {
        TrainerRequestDTO request = mock(TrainerRequestDTO.class);
        TrainingType type = mock(TrainingType.class);
        User user = User.builder().username("tom.tomas").password("encoded").isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(request.getFirstName()).thenReturn("Tom");
        when(request.getLastName()).thenReturn("Tomas");
        when(request.getSpecialization()).thenReturn("FITNESS");
        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(userProfileService.generateUsername("Tom", "Tomas")).thenReturn("tom.tomas");
        when(userProfileService.generatePassword()).thenReturn("pass");
        when(trainingTypeRepository.findByTrainingTypeName("FITNESS")).thenReturn(Optional.of(type));
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(mapper.toEntity(request)).thenReturn(trainer);
        when(trainerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreatedTrainer result = service.createTrainer(request);

        assertThat(result).isNotNull();
        verify(trainerRepository).save(any());
    }

    @Test
    void updateTrainer_shouldReturnDto() {
        TrainerUpdateDTO request = mock(TrainerUpdateDTO.class);
        TrainingType type = mock(TrainingType.class);
        TrainerResponseDTO dto = mock(TrainerResponseDTO.class);
        User user = User.builder().username("user1").firstName("Old").lastName("Name").build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(request.getUsername()).thenReturn("user1");
        when(request.getFirstName()).thenReturn("New");
        when(request.getLastName()).thenReturn("Name");
        when(request.getIsActive()).thenReturn(true);
        when(request.getSpecialization()).thenReturn("FITNESS");
        when(trainerRepository.findByUser_Username("user1")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName("FITNESS")).thenReturn(Optional.of(type));
        when(trainerRepository.save(any())).thenReturn(trainer);
        when(mapper.toDto(any())).thenReturn(dto);

        TrainerResponseDTO result = service.updateTrainer(request);

        assertThat(result).isNotNull();
        verify(trainerRepository).save(any());
    }

    @Test
    void getTrainerByUsername_shouldReturnDto() {
        Trainer trainer = mock(Trainer.class);
        TrainerInfoDTO dto = mock(TrainerInfoDTO.class);

        when(trainerRepository.findByUser_Username("user")).thenReturn(Optional.of(trainer));
        when(mapper.toInfoDto(trainer)).thenReturn(dto);

        TrainerInfoDTO result = service.getTrainerByUsername("user");

        assertThat(result).isNotNull();
    }

    @Test
    void changePassword_shouldUpdatePassword() throws AuthenticationException {
        User user = User.builder().username("user").password("old").isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUser_Username("user")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("oldPass", "old")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");

        service.changePassword("user", "oldPass", "newPass");

        verify(trainerRepository).save(any());
    }

    @Test
    void changePassword_shouldThrow_whenWrongPassword() {
        User user = User.builder().username("user").password("old").isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUser_Username("user")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("bad", "old")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword("user", "bad", "new")).isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void setActive_shouldSaveTrainer() {
        User user = User.builder().username("user").isActive(false).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUser_Username("user")).thenReturn(Optional.of(trainer));

        service.setActive("user", true);

        verify(trainerRepository).save(any());
    }

    @Test
    void getTrainings_shouldReturnList() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().username("Callum.Whitfield").fromDate(LocalDate.of(2024, 1, 1)).toDate(LocalDate.of(2024, 12, 31)).build();

        when(trainingRepository.findByTrainerCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate())).thenReturn(List.of());

        List<Training> result = service.getTrainings(filter);

        assertThat(result).isEmpty();
        verify(trainingRepository).findByTrainerCriteria(filter.getUsername(), filter.getFromDate(), filter.getToDate());
    }

    @Test
    void getNotAssignedToTrainee_shouldReturnList() {
        TrainerInfoDTO dto = mock(TrainerInfoDTO.class);

        when(trainerRepository.findAllNotAssignedToTrainee("user")).thenReturn(List.of(dto));

        List<TrainerInfoDTO> result = service.getNotAssignedToTrainee("user");

        assertThat(result).isNotNull().hasSize(1);
    }

    @Test
    void updateProfile_shouldUpdateUserFieldsOnly() {
        User existingUser = User.builder()
                .username("trainer1")
                .firstName("Old")
                .lastName("Name")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder().user(existingUser).specialization(TrainingType.builder().trainingTypeName("FITNESS").build()).build();
        User incomingUser = User.builder().firstName("New").lastName("Surname").build();
        Trainer updatedData = Trainer.builder().user(incomingUser).build();

        when(trainerRepository.findByUser_Username("trainer1")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.updateProfile("trainer1", updatedData);

        assertThat(result.getUser().getFirstName()).isEqualTo("New");
        assertThat(result.getUser().getLastName()).isEqualTo("Surname");
        assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo("FITNESS");
        verify(trainerRepository).save(any());
    }

    @Test
    void updateProfile_shouldUpdateSpecializationOnly() {
        User existingUser = User.builder()
                .username("trainer1")
                .firstName("Old")
                .lastName("Name")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder().user(existingUser).specialization(TrainingType.builder().trainingTypeName("FITNESS").build()).build();
        Trainer updatedData = Trainer.builder().specialization(TrainingType.builder().trainingTypeName("CROSSFIT").build()).build();

        when(trainerRepository.findByUser_Username("trainer1")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.updateProfile("trainer1", updatedData);

        assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo("CROSSFIT");
        assertThat(result.getUser().getFirstName()).isEqualTo("Old");
        assertThat(result.getUser().getLastName()).isEqualTo("Name");
        verify(trainerRepository).save(any());
    }

    @Test
    void updateProfile_shouldNotChangeUser_whenUserIsNull() {
        User existingUser = User.builder()
                .username("trainer1")
                .firstName("Old")
                .lastName("Name")
                .isActive(true).build();
        Trainer trainer = Trainer.builder().user(existingUser).specialization(TrainingType.builder().trainingTypeName("FITNESS").build()).build();
        Trainer updatedData = Trainer.builder().specialization(TrainingType.builder().trainingTypeName("CROSSFIT").build()).build();

        when(trainerRepository.findByUser_Username("trainer1")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.updateProfile("trainer1", updatedData);

        assertThat(result.getUser().getFirstName()).isEqualTo("Old");
        assertThat(result.getUser().getLastName()).isEqualTo("Name");
        assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo("CROSSFIT");

        verify(trainerRepository).save(any());
    }

    @Test
    void setActive_shouldThrow_whenAlreadyActive() {
        User user = User.builder().username("trainer1").isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUser_Username("trainer1")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> service.setActive("trainer1", true)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainer 'trainer1' is already active. No action taken.");
        verify(trainerRepository).findByUser_Username("trainer1");
    }

    @Test
    void setActive_shouldThrow_whenAlreadyInactive() {
        User user = User.builder().username("trainer1").isActive(false).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUser_Username("trainer1")).thenReturn(Optional.of(trainer));
        assertThatThrownBy(() -> service.setActive("trainer1", false)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainer 'trainer1' is already inactive. No action taken.");

        verify(trainerRepository).findByUser_Username("trainer1");
    }
}