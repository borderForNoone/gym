package org.gym.crm.model;

public enum FieldName {
    FIRST_NAME("First name"),
    LAST_NAME("Last name"),
    USERNAME("Username"),
    PASSWORD("Password"),

    DATE_OF_BIRTH("Date of birth"),
    ADDRESS("Address"),

    SPECIALIZATION("Specialization"),

    TRAINING_NAME("Training name"),
    TRAINING_DATE("Training date"),
    TRAINING_DURATION("Training duration"),
    TRAINING_TYPE("Training type"),

    TRAINEE("Trainee"),
    TRAINER("Trainer"),
    TRAINING("Training"),
    USER("User");

    private final String displayName;

    FieldName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
