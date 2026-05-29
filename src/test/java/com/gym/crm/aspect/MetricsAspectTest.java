package com.gym.crm.aspect;

import com.gym.crm.actuator.metrics.LoginMetrics;
import com.gym.crm.actuator.metrics.RegistrationMetrics;
import com.gym.crm.actuator.metrics.TrainingMetrics;
import com.gym.crm.exception.BadCredentialsException;
import com.gym.crm.facade.dto.AuthResponseDTO;
import com.gym.crm.facade.dto.TraineeResponseDTO;
import com.gym.crm.facade.dto.TrainerResponseDTO;
import com.gym.crm.facade.dto.TrainingResponseDTO;
import com.gym.crm.util.TestConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsAspectTest {
    private final TraineeResponseDTO traineeResponseDTO = TestConstants.buildTraineeResponseDTO();
    private final TrainerResponseDTO trainerResponseDTO = TestConstants.buildTrainerResponseDTO();
    private final TrainingResponseDTO trainingResponseDTO = TestConstants.buildTrainingResponseDTO();
    private final AuthResponseDTO authResponseDTO = TestConstants.buildAuthResponseDTO();

    @Mock
    private RegistrationMetrics registrationMetrics;
    @Mock
    private LoginMetrics loginMetrics;
    @Mock
    private TrainingMetrics trainingMetrics;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private MetricsAspect aspect;

    @Test
    void trackTraineeRegistration_shouldIncrementSuccess_whenNoException() throws Throwable {
        when(joinPoint.proceed()).thenReturn(traineeResponseDTO);

        aspect.trackTraineeRegistration(joinPoint);

        verify(registrationMetrics).incrementTraineeCount(true);
        verify(registrationMetrics, never()).incrementTraineeCount(false);
    }

    @Test
    void trackTraineeRegistration_shouldIncrementFailure_whenExceptionThrown() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        assertThrows(RuntimeException.class, () -> aspect.trackTraineeRegistration(joinPoint));

        verify(registrationMetrics).incrementTraineeCount(false);
        verify(registrationMetrics, never()).incrementTraineeCount(true);
    }

    @Test
    void trackTrainerRegistration_shouldIncrementSuccess_whenNoException() throws Throwable {
        when(joinPoint.proceed()).thenReturn(trainerResponseDTO);

        aspect.trackTrainerRegistration(joinPoint);

        verify(registrationMetrics).incrementTrainerCount(true);
        verify(registrationMetrics, never()).incrementTrainerCount(false);
    }

    @Test
    void trackTrainerRegistration_shouldIncrementFailure_whenExceptionThrown() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        assertThrows(RuntimeException.class, () -> aspect.trackTrainerRegistration(joinPoint));

        verify(registrationMetrics).incrementTrainerCount(false);
        verify(registrationMetrics, never()).incrementTrainerCount(true);
    }

    @Test
    void trackLogin_shouldIncrementSuccess_whenNoException() throws Throwable {
        when(joinPoint.proceed()).thenReturn(authResponseDTO);

        aspect.trackLogin(joinPoint);

        verify(loginMetrics).incrementCount(true);
        verify(loginMetrics, never()).incrementCount(false);
    }

    @Test
    void trackLogin_shouldIncrementFailure_whenExceptionThrown() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new BadCredentialsException("invalid"));

        assertThrows(BadCredentialsException.class, () -> aspect.trackLogin(joinPoint));

        verify(loginMetrics).incrementCount(false);
        verify(loginMetrics, never()).incrementCount(true);
    }

    @Test
    void trackTrainingCreation_shouldIncrementCounter_whenResultIsTrainingResponseDTO() throws Throwable {
        when(joinPoint.proceed()).thenReturn(trainingResponseDTO);

        aspect.trackTrainingCreation(joinPoint);

        verify(trainingMetrics).incrementCounter("Cardio");
    }

    @Test
    void trackTrainingCreation_shouldNotIncrementCounter_whenResultIsNotTrainingResponseDTO() throws Throwable {
        when(joinPoint.proceed()).thenReturn(null);

        aspect.trackTrainingCreation(joinPoint);

        verify(trainingMetrics, never()).incrementCounter(any());
    }
}