package org.gym.crm.search.filter;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
public abstract class TrainingFilter {
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate fromDate;
    private LocalDate toDate;
}
