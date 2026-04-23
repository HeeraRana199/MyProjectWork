package com.cts.controller;

import com.cts.dto.LoginRequest;
import com.cts.dto.LoginResponse;
import com.cts.entity.Candidate;
import com.cts.entity.User;
import com.cts.repository.CandidateRepository;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtils;
import com.cts.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@AllArgsConstructor
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    private AuthService authService;
    private CandidateRepository candidateRepository;

    @PostMapping("/registration")
    public ResponseEntity<User> register(@RequestBody User user) {
        // Authenticate using Spring Security (validates username + password via BCrypt)
        User savedUser = authService.registration(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
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
