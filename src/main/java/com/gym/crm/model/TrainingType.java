package com.gym.crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
@ToString(exclude = "trainings")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Entity
@Table(name = "training_types")
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true, length = 100)
    private String trainingTypeName;

    @OneToMany(mappedBy = "trainingType")
    private Set<Training> trainings = new HashSet<>();
}
