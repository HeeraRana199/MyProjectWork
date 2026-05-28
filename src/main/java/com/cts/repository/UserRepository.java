package com.cts.repository;

import com.cts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Repository for user authentication records */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /** Most recently created first — used by the Admin leader management list. */
    List<User> findByRoleOrderByUserIdDesc(User.Role role);
}
