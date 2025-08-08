package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"user\"")
public class AuthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    @Column(name = "password", nullable = false, length = 100) // Increased length for bcrypt
    private String password;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "force_password_reset", nullable = false)
    private boolean forcePasswordReset = false;

    @ManyToMany(mappedBy = "auditors")
    private Set<Audit> audits = new HashSet<>();

    @OneToMany(mappedBy = "uploaded_by")
    private Set<Document> documents = new HashSet<>();

    @OneToMany(mappedBy = "assigned_auditor")
    private Set<Plan> plans = new HashSet<>();

    @OneToMany(mappedBy = "reviewer")
    private Set<Instance> reviewedInstances = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
