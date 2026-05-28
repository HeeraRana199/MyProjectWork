package com.cts.service;


import com.cts.entity.User;
import com.cts.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    public User registration(User user){

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public User leaderRegister(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.ROLE_LEADER);
        return userRepository.save(user);
    }

    /** List every account with role ROLE_LEADER (most recent first). */
    public java.util.List<User> listLeaders() {
        return userRepository.findByRoleOrderByUserIdDesc(User.Role.ROLE_LEADER);
    }

    /** Delete a leader account. Throws if the user doesn't exist or isn't a leader. */
    public void deleteLeader(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != User.Role.ROLE_LEADER) {
            throw new IllegalArgumentException("Account is not a leader — refusing to delete via this endpoint");
        }
        userRepository.delete(user);
    }

    /**
     * Change the password for an authenticated user.
     * Verifies the current password via BCrypt before updating.
     */
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
