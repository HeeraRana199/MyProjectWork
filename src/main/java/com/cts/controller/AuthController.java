package com.cts.controller;

import com.cts.dto.ChangePasswordRequest;
import com.cts.dto.LoginRequest;
import com.cts.dto.LoginResponse;
import com.cts.entity.Candidate;
import com.cts.entity.User;
import com.cts.repository.CandidateRepository;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtils;
import com.cts.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthController - Handles login for all user types (Admin, Trainee, Leader).
 *
 * POST /api/auth/login
 *   → Validates credentials
 *   → Returns JWT token + role info
 *
 * The React frontend stores this JWT and sends it in every subsequent request
 * as: Authorization: Bearer <token>
 */

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "Login, registration, change-password — no JWT required (except change-password, which reads the SecurityContext set by AuthTokenFilter).")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    private AuthService authService;
    private CandidateRepository candidateRepository;

    @PostMapping("/registration")
    @SecurityRequirements({}) // public — no JWT required
    @Operation(summary = "Register a new Admin account",
            description = "Creates a new ROLE_ADMIN user with a BCrypt-hashed password.")
    public ResponseEntity<User> register(@RequestBody @Valid User user) {
        // Authenticate using Spring Security (validates username + password via BCrypt)
        User savedUser = authService.registration(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    /**
     * Change password for the currently authenticated user.
     * The user is identified from the JWT (populated into SecurityContext by AuthTokenFilter).
     * This endpoint is publicly mapped at the Spring Security layer; auth is enforced here
     * so the response is always a structured JSON body (Spring Security's default 403 is empty).
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change current user's password",
            description = "Requires a valid JWT (extracted from the SecurityContext). Validates the old password, then updates to the new one if it differs and meets length requirements.")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Change-password request received. Authentication present: {}",
                auth != null && auth.isAuthenticated());

        String email = null;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails ud) {
                email = ud.getUsername();
            } else if (principal instanceof String s && !"anonymousUser".equalsIgnoreCase(s)) {
                email = s;
            }
        }

        if (email == null) {
            logger.warn("Change-password rejected — no authenticated principal in SecurityContext");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of(
                            "message", "Not authenticated. Please log in again.",
                            "errors", java.util.List.of()
                    ));
        }

        try {
            authService.changePassword(email, request.getOldPassword(), request.getNewPassword());
            logger.info("Password updated successfully for {}", email);
            return ResponseEntity.ok(java.util.Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.info("Change-password rejected for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of(
                            "message", e.getMessage(),
                            "errors", java.util.List.of()
                    ));
        }
    }

    @PostMapping("/login")
    @SecurityRequirements({}) // public — no JWT required
    @Operation(summary = "Authenticate and obtain a JWT",
            description = "Validates email + password against Spring Security. On success returns `{ token, email, role, associateId }`. The `token` is a JWT to be sent as `Authorization: Bearer <token>` on subsequent requests.")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Authenticate using Spring Security (validates username + password via BCrypt)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get authenticated user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(userDetails.getUsername());

        // Fetch full user from DB to get role + associateId
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Candidate candidate = candidateRepository.findByCognizantEmailID(request.getEmail()).orElse(null);
        Integer associateId =null;
        if(candidate!=null)
            associateId=  candidate.getAssociateId();

        // Build response - include associateId so frontend knows which profile to load
        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .associateId(associateId)
                .build();

        return ResponseEntity.ok(response);
    }
}
