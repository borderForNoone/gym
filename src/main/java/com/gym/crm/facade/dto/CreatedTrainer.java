package com.gym.crm.facade.dto;

import com.gym.crm.model.Trainer;

public record CreatedTrainer(Trainer trainer, String rawPassword) {}
