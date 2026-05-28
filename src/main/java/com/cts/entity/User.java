package com.cts.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "Email should not be empty")
    @Email(message = "Email must be a valid email address")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * BCrypt-hashed password.
     * WRITE_ONLY = accepted from request bodies (registration / leaderRegister)
     * but never serialized back to clients (leader list, login response, etc.).
     * NB: @JsonIgnore would also block deserialization — that's why we use the
     * write-only access mode instead.
     */
    @NotNull(message = "Password should not be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * Role of the user:
     * ROLE_ADMIN - Can upload Excel, manage all data
     * ROLE_TRAINEE - Can view own profile and edit limited fields
     * ROLE_LEADER - Can view all trainees, filter/search
     */
    @NotNull(message = "Role should not be empty")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Whether this user account is active */
    @Builder.Default
    private boolean active = true;

    @OneToOne
    @JoinColumn(name = "associate_id", referencedColumnName = "associate_id")
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
