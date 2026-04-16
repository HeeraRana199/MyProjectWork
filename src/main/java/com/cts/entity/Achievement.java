package com.cts.entity;


import jakarta.persistence.*;
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

    private String type; // "ACHIEVEMENT" or "ACTIVITY"

    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "cand_id")
    private Candidate candidate;
}

