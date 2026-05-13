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
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    private AuthService authService;
    private CandidateRepository candidateRepository;

    @PostMapping("/registration")
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

        // Fetch full user from DB to get role + candidateId
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Candidate candidate = candidateRepository.findByCognizantEmailID(request.getEmail()).orElse(null);
        Integer candidateid =null;
        if(candidate!=null)
            candidateid=  candidate.getCognizantCandidateId();

        // Build response - include candidateId so frontend knows which profile to load
        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .candidateId(candidateid)
                .build();

        return ResponseEntity.ok(response);
    }
}
