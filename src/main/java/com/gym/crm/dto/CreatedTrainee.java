package com.gym.crm.dto;

import com.gym.crm.model.Trainee;

public record CreatedTrainee(Trainee trainee, String rawPassword) {}