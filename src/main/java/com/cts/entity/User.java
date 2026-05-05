package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * User Entity - Stores login credentials and role for each user.
 * Roles: ADMIN, TRAINEE, LEADER
 *
 * Admins are created manually or seeded.
 * Trainees are auto-created when Admin uploads Excel.
 * Leaders are created by Admin from the Admin Panel.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    /** Username = cognizantEmail for trainees */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt-hashed password */
    @Column(nullable = false)
    private String password;

    /**
     * Role of the user:
     * ROLE_ADMIN - Can upload Excel, manage all data
     * ROLE_TRAINEE - Can view own profile and edit limited fields
     * ROLE_LEADER - Can view all trainees, filter/search
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Whether this user account is active */
    @Builder.Default
    private boolean active = true;

    @OneToOne
    @JoinColumn(name = "candidate_id", referencedColumnName = "cognizant_candidate_id")
    private Candidate candidate;





    /**
     * One-to-One: Trainees have a linked Candidate profile.
     * Admin and Leader may not have a Candidate linked.
     */


    /** Enum for roles */
    public enum Role {
        ROLE_ADMIN,
        ROLE_TRAINEE,
        ROLE_LEADER
    }
}
