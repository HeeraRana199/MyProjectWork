package com.cts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Certification {
    @Id
    private String certificationId;
    private String certificationName;
    private String certificationProvider;
    private Boolean status;
    @ManyToOne
    @JoinColumn(name="candidate_id")
    private Candidate candidate;
}
