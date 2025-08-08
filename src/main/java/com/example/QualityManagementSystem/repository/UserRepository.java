package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByUsername(String username);
    Optional<AuthUser> findByEmail(String email);
    List<AuthUser> findByRole(Role role);
    List<AuthUser> findByIsActiveTrue();
}
