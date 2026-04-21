package com.cts.controller;

import com.cts.dto.LoginRequest;
import com.cts.dto.LoginResponse;
import com.cts.entity.User;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;

    /**
     * Login endpoint - public, no authentication required.
     *
     * Request body: { "username": "admin@cognizant.com", "password": "Admin@1234" }
     * Response:     { "token": "eyJ...", "role": "ROLE_ADMIN", "name": "Admin" }
     */



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

        // Build response - include candidateId so frontend knows which profile to load
        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registration")
    public ResponseEntity<User> register(@RequestBody User user) {
        // Authenticate using Spring Security (validates username + password via BCrypt)
        User savedUser = userRepository.save(user);


        return ResponseEntity.ok(savedUser);
    }


}
