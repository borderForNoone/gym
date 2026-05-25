package com.gym.crm.service.impl;

import com.gym.crm.dto.CreatedTrainer;
import com.gym.crm.dto.TrainerInfoDTO;
import com.gym.crm.dto.TrainerRequestDTO;
import com.gym.crm.dto.TrainerResponseDTO;
import com.gym.crm.dto.TrainerUpdateDTO;
import com.gym.crm.exception.InvalidPasswordException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.UserInputValidator;
import com.gym.crm.util.CoreValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private TrainerTrainingCriteriaBuilder criteriaBuilder;
    @Mock
    private CoreValidator validator;
    @Mock
    private UserInputValidator userInputValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TrainerMapper mapper;
    @Mock
    private EntityManager entityManager;
    @Mock
    private CriteriaBuilder cb;
    @Mock
    private CriteriaQuery<Training> cq;
    @Mock
    private TypedQuery<Training> typedQuery;

    @InjectMocks
    private TrainerServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
    }

    @Test
    void createTrainer_shouldReturnCreatedTrainer() {
        TrainerRequestDTO request = mock(TrainerRequestDTO.class);
        TrainingType type = mock(TrainingType.class);
        User user = User.builder().username("john.doe").password("encoded").isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(request.getFirstName()).thenReturn("John");
        when(request.getLastName()).thenReturn("Doe");
        when(request.getSpecialization()).thenReturn("FITNESS");
        doNothing().when(userInputValidator).validate(request, "TRAINER");
        when(userProfileService.generateUsername("John", "Doe")).thenReturn("john.doe");
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
        User user = User.builder().username("user1").firstName("Old").lastName("Name").isActive(false).build();
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
        TrainerTrainingFilter filter = mock(TrainerTrainingFilter.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(criteriaBuilder.build(cb, filter)).thenReturn(cq);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        List<Training> result = service.getTrainings(filter);

        assertThat(result).isEmpty();
    }

    @Test
    void getNotAssignedToTrainee_shouldReturnList() {
        User user = User.builder().username("other").isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).trainees(Set.of()).build();

        when(trainerRepository.findAll()).thenReturn(List.of(trainer));
        when(mapper.toInfoDto(trainer)).thenReturn(mock(TrainerInfoDTO.class));

        List<TrainerInfoDTO> result = service.getNotAssignedToTrainee("user");

        assertThat(result).isNotNull();
    }
}