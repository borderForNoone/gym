package org.gym.crm.storage.parser;

import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CsvParser {
    public Trainee parseTrainee(String[] fields) {
        return Trainee.builder()
                .id(Long.parseLong(fields[8]))
                .user(User.builder()
                        .firstName(fields[1])
                        .lastName(fields[2])
                        .username(fields[3])
                        .password(fields[4])
                        .isActive(Boolean.parseBoolean(fields[5]))
                        .build())
                .dateOfBirth(fields[6].isBlank() ? null : LocalDate.parse(fields[6]))
                .address(fields[7])
                .build();
    }

    public Trainer parseTrainer(String[] fields) {
        TrainingType specialization = TrainingType.builder()
                .trainingTypeName(fields[6])
                .build();

        return Trainer.builder()
                .id(Long.parseLong(fields[7]))
                .user(User.builder()
                        .firstName(fields[1])
                        .lastName(fields[2])
                        .username(fields[3])
                        .password(fields[4])
                        .isActive(Boolean.parseBoolean(fields[5]))
                        .build())
                .specialization(specialization)
                .build();
    }

    public Training parseTraining(String[] fields) {
        TrainingType trainingType = TrainingType.builder()
                .trainingTypeName(fields[4])
                .build();

        Trainee trainee = Trainee.builder()
                .id(Long.parseLong(fields[1]))
                .build();

        Trainer trainer = Trainer.builder()
                .id(Long.parseLong(fields[2]))
                .build();

        return Training.builder()
                .id(Long.parseLong(fields[0]))
                .trainingName(fields[3])
                .trainingType(trainingType)
                .trainingDate(LocalDate.parse(fields[5]))
                .trainingDuration(Integer.parseInt(fields[6]))
                .trainee(trainee)
                .trainer(trainer)
                .build();
    }
}
