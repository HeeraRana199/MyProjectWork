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
public class Certification {
    @Id
    @NotNull(message = "Certification ID should not be empty")
    private String certificationId;

    @NotNull(message = "Certification name should not be empty")
    private String certificationName;

    @NotNull(message = "Certification provider should not be empty")
    private String certificationProvider;

    private Boolean status;

    @ManyToOne
    @JoinColumn(name="associate_id")
    private Candidate candidate;
}
