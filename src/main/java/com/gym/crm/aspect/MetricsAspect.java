package com.gym.crm.aspect;

import com.gym.crm.actuator.metrics.LoginMetrics;
import com.gym.crm.actuator.metrics.RegistrationMetrics;
import com.gym.crm.actuator.metrics.TrainingMetrics;
import com.gym.crm.facade.dto.TrainingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAspect {

    private final RegistrationMetrics registrationMetrics;
    private final LoginMetrics loginMetrics;
    private final TrainingMetrics trainingMetrics;

    @Around("execution(* com.gym.crm.service.impl.TraineeServiceImpl.createTrainee(..))")
    public Object trackTraineeRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            registrationMetrics.incrementTraineeCount(true);

            return result;
        } catch (Throwable t) {
            registrationMetrics.incrementTraineeCount(false);
            throw t;
        }
    }

    @Around("execution(* com.gym.crm.service.impl.TrainerServiceImpl.createTrainer(..))")
    public Object trackTrainerRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            registrationMetrics.incrementTrainerCount(true);

            return result;
        } catch (Throwable t) {
            registrationMetrics.incrementTrainerCount(false);
            throw t;
        }
    }

    @Around("execution(* com.gym.crm.service.impl.TrainingServiceImpl.createTraining(..))")
    public Object trackTrainingCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result instanceof TrainingResponseDTO dto) {
            trainingMetrics.incrementCounter(dto.getTrainingTypeName());
        }

        return result;
    }

    @Around("execution(* com.gym.crm.service.common.AuthenticationService.authenticate(..))")
    public Object trackLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            loginMetrics.incrementCount(true);

            return result;
        } catch (Throwable t) {
            loginMetrics.incrementCount(false);
            throw t;
        }
    }
}