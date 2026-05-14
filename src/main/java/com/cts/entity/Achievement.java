package com.cts.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aId;

    @NotNull(message = "Type should not be empty (ACHIEVEMENT or ACTIVITY)")
    private String type; // "ACHIEVEMENT" or "ACTIVITY"

    @NotNull(message = "Title should not be empty")
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "associate_id")
    private Candidate candidate;
}
