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
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    @NotNull(message = "Project name should not be empty")
    private String projectName;
    @NotNull(message = "Tech should not be empty")
    private String tech;
    @NotNull(message = "Outcome should not be empty")
    private String outcome;
    @NotNull(message = "Role should not be empty")
    private String role;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "associate_id")
    private Candidate candidate;
}
