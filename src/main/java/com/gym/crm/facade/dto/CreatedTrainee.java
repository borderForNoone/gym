package com.gym.crm.facade.dto;

import com.gym.crm.model.Trainee;

public record CreatedTrainee(Trainee trainee, String rawPassword) {}