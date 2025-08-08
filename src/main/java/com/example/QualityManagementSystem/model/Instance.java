package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_clause_instance")
public class Instance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instance_id")
    private long instance_id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "audit_id", referencedColumnName = "audit_id")
    private Audit audit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clause_id", referencedColumnName = "clause_id")
    private Clause_library clause;

    @Enumerated(EnumType.STRING)
    @Column(name = "conformity_status", nullable = false)
    private ConformityStatus conformity_status;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewer_id", referencedColumnName = "user_id")
    private AuthUser reviewer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", referencedColumnName = "plan_id")
    private Plan plan;

    @Column(name = "validation_comments", columnDefinition = "TEXT")
    private String validation_comments;

    @Column(name = "validated_at")
    private LocalDateTime validated_at;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();
}
