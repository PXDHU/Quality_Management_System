package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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

    @Column(name = "password", nullable = false, length = 100)
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

    // AuthUser ↔ Audit (as auditor)
    @ManyToMany(mappedBy = "auditors")
    @JsonIgnore
    private List<Audit> audits = new ArrayList<>();

    // AuthUser ↔ Document (as uploader)
    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<Document> uploadedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "assignedAuditor")
    @JsonIgnore
    private List<Plan> plans = new ArrayList<>();

    @OneToMany(mappedBy = "reviewer")
    @JsonIgnore
    private List<Instance> reviewedInstances = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthUser that = (AuthUser) o;
        return userId != null && userId.equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
