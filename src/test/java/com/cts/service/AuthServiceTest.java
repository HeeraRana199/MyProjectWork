package com.cts.service;

import com.cts.entity.User;
import com.cts.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthService authService;

    private User newAdminUserInput() {
        User u = new User();
        u.setEmail("admin@cognizant.com");
        u.setPassword("plain-pass");
        u.setRole(User.Role.ROLE_ADMIN);
        return u;
    }

    // ── registration ─────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ registration() encodes the password and persists the user")
    void registration_encodes_and_saves() {
        User input = newAdminUserInput();
        when(passwordEncoder.encode("plain-pass")).thenReturn("BCRYPT_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.registration(input);

        assertThat(saved.getPassword()).isEqualTo("BCRYPT_HASH");
        assertThat(saved.getEmail()).isEqualTo("admin@cognizant.com");
        assertThat(saved.getRole()).isEqualTo(User.Role.ROLE_ADMIN);
        verify(passwordEncoder, times(1)).encode("plain-pass");
        verify(userRepository, times(1)).save(input);
    }

    // ── leaderRegister ───────────────────────────────────────────────────

    @Test
    @DisplayName("✓ leaderRegister() forces ROLE_LEADER regardless of input role")
    void leader_register_forces_role() {
        User input = newAdminUserInput(); // pretends to be admin
        when(passwordEncoder.encode("plain-pass")).thenReturn("BCRYPT_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.leaderRegister(input);

        assertThat(saved.getRole()).isEqualTo(User.Role.ROLE_LEADER);
        assertThat(saved.getPassword()).isEqualTo("BCRYPT_HASH");
    }

    // ── changePassword ───────────────────────────────────────────────────

    @Test
    @DisplayName("✓ changePassword() updates when current matches and new differs")
    void change_password_happy_path() {
        User existing = new User();
        existing.setEmail("user@cognizant.com");
        existing.setPassword("OLD_HASH");

        when(userRepository.findByEmail("user@cognizant.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("old", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.matches("newpass", "OLD_HASH")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("NEW_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.changePassword("user@cognizant.com", "old", "newpass");

        assertThat(existing.getPassword()).isEqualTo("NEW_HASH");
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("✗ changePassword() rejects when current password is wrong")
    void change_password_wrong_old() {
        User existing = new User();
        existing.setEmail("user@cognizant.com");
        existing.setPassword("OLD_HASH");

        when(userRepository.findByEmail("user@cognizant.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("WRONG", "OLD_HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword("user@cognizant.com", "WRONG", "newpass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("✗ changePassword() rejects when new password equals current")
    void change_password_same_new_old() {
        User existing = new User();
        existing.setEmail("user@cognizant.com");
        existing.setPassword("OLD_HASH");

        when(userRepository.findByEmail("user@cognizant.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("same", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.matches("same", "OLD_HASH")).thenReturn(true); // both checks pass

        assertThatThrownBy(() -> authService.changePassword("user@cognizant.com", "same", "same"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("✗ changePassword() rejects when new password is shorter than 6 chars")
    void change_password_too_short() {
        User existing = new User();
        existing.setEmail("user@cognizant.com");
        existing.setPassword("OLD_HASH");

        when(userRepository.findByEmail("user@cognizant.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("oldpass", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.matches("abc", "OLD_HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword("user@cognizant.com", "oldpass", "abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 6 characters");
    }

    @Test
    @DisplayName("✗ changePassword() throws when user not found")
    void change_password_no_user() {
        when(userRepository.findByEmail("ghost@cognizant.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword("ghost@cognizant.com", "x", "y"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ── listLeaders ──────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ listLeaders() filters by ROLE_LEADER and returns most-recent first")
    void list_leaders_delegates_to_repository() {
        User l1 = new User(); l1.setUserId(1L); l1.setRole(User.Role.ROLE_LEADER);
        User l2 = new User(); l2.setUserId(2L); l2.setRole(User.Role.ROLE_LEADER);
        when(userRepository.findByRoleOrderByUserIdDesc(User.Role.ROLE_LEADER))
                .thenReturn(List.of(l2, l1));

        List<User> result = authService.listLeaders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(2L);
    }

    // ── deleteLeader ─────────────────────────────────────────────────────

    @Test
    @DisplayName("✓ deleteLeader() removes a leader account by id")
    void delete_leader_happy_path() {
        User leader = new User();
        leader.setUserId(42L);
        leader.setRole(User.Role.ROLE_LEADER);
        when(userRepository.findById(42L)).thenReturn(Optional.of(leader));

        authService.deleteLeader(42L);

        verify(userRepository, times(1)).delete(leader);
    }

    @Test
    @DisplayName("✗ deleteLeader() refuses to delete a non-leader account")
    void delete_leader_refuses_non_leader() {
        User admin = new User();
        admin.setUserId(99L);
        admin.setRole(User.Role.ROLE_ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> authService.deleteLeader(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a leader");

        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("✗ deleteLeader() throws when user id doesn't exist")
    void delete_leader_not_found() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.deleteLeader(404L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
