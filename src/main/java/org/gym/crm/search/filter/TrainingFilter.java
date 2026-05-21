package org.gym.crm.search.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
@EqualsAndHashCode
public abstract class TrainingFilter {
    private String username;
    private String joinFullName;
    private LocalDate fromDate;
    private LocalDate toDate;
}
