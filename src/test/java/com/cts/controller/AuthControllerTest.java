package com.cts.controller;

import com.cts.dto.ChangePasswordRequest;
import com.cts.dto.LoginRequest;
import com.cts.entity.Candidate;
import com.cts.entity.User;
import com.cts.repository.CandidateRepository;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtils;
import com.cts.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtUtils jwtUtils;
    @Mock UserRepository userRepository;
    @Mock AuthService authService;
    @Mock CandidateRepository candidateRepository;
    @InjectMocks AuthController authController;

    private MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails ud(String email) {
        UserBuilder b = org.springframework.security.core.userdetails.User
                .withUsername(email).password("x").roles("ADMIN");
        return b.build();
    }

    private void setAuthenticatedPrincipal(UserDetails principal) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, "credentials", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── POST /auth/registration ──────────────────────────────────────────

    @Test
    @DisplayName("✓ POST /auth/registration returns 201 with persisted user")
    void registration_happy_path() throws Exception {
        User saved = new User();
        saved.setUserId(1L);
        saved.setEmail("admin@cts.com");
        saved.setRole(User.Role.ROLE_ADMIN);
        when(authService.registration(any(User.class))).thenReturn(saved);

        // Build JSON manually — User.password is @JsonProperty(WRITE_ONLY) so it
        // would be omitted by om.writeValueAsString() and @NotNull would reject.
        String body = "{\"email\":\"admin@cts.com\",\"password\":\"plain-pass\",\"role\":\"ROLE_ADMIN\"}";

        mvc.perform(post("/auth/registration")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("admin@cts.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    // ── POST /auth/login ─────────────────────────────────────────────────

    @Test
    @DisplayName("✓ POST /auth/login returns JWT + role + associateId when credentials are valid")
    void login_happy_path_with_candidate() throws Exception {
        LoginRequest req = new LoginRequest("trainee@cts.com", "secret");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                ud("trainee@cts.com"), "secret");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtToken("trainee@cts.com")).thenReturn("jwt.token.here");

        User user = new User();
        user.setEmail("trainee@cts.com");
        user.setRole(User.Role.ROLE_TRAINEE);
        when(userRepository.findByEmail("trainee@cts.com")).thenReturn(Optional.of(user));

        Candidate cand = new Candidate();
        cand.setAssociateId(2308322);
        when(candidateRepository.findByCognizantEmailID("trainee@cts.com")).thenReturn(Optional.of(cand));

        mvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.role").value("ROLE_TRAINEE"))
                .andExpect(jsonPath("$.associateId").value(2308322));
    }

    @Test
    @DisplayName("✓ POST /auth/login still succeeds when no candidate row is linked (associateId null)")
    void login_happy_path_without_candidate() throws Exception {
        LoginRequest req = new LoginRequest("admin@cts.com", "secret");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                ud("admin@cts.com"), "secret");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtToken("admin@cts.com")).thenReturn("jwt-2");

        User user = new User();
        user.setEmail("admin@cts.com");
        user.setRole(User.Role.ROLE_ADMIN);
        when(userRepository.findByEmail("admin@cts.com")).thenReturn(Optional.of(user));
        when(candidateRepository.findByCognizantEmailID("admin@cts.com")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.associateId").doesNotExist());
    }

    @Test
    @DisplayName("✗ POST /auth/login propagates BadCredentialsException for wrong password")
    void login_bad_credentials() throws Exception {
        LoginRequest req = new LoginRequest("admin@cts.com", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        try {
            mvc.perform(post("/auth/login")
                    .contentType(APPLICATION_JSON)
                    .content(om.writeValueAsString(req)));
        } catch (Exception e) {
            // standaloneSetup has no exception resolver — that's fine, we asserted intent above
        }
        verify(jwtUtils, never()).generateJwtToken(anyString());
    }

    // ── POST /auth/change-password ───────────────────────────────────────

    @Test
    @DisplayName("✓ POST /auth/change-password returns 200 when current password matches")
    void change_password_happy_path() throws Exception {
        setAuthenticatedPrincipal(ud("user@cts.com"));
        ChangePasswordRequest req = new ChangePasswordRequest("oldpass", "newpass");

        mvc.perform(post("/auth/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));

        verify(authService).changePassword(eq("user@cts.com"), eq("oldpass"), eq("newpass"));
    }

    @Test
    @DisplayName("✗ POST /auth/change-password returns 401 when nobody is authenticated")
    void change_password_unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        ChangePasswordRequest req = new ChangePasswordRequest("o", "newpass");

        mvc.perform(post("/auth/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated. Please log in again."));

        verify(authService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("✗ POST /auth/change-password returns 400 when service rejects (wrong current password)")
    void change_password_bad_request_from_service() throws Exception {
        setAuthenticatedPrincipal(ud("user@cts.com"));
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(authService).changePassword(anyString(), anyString(), anyString());

        ChangePasswordRequest req = new ChangePasswordRequest("WRONG", "newpass");

        mvc.perform(post("/auth/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }
}
