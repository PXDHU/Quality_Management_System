package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit")
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private long auditId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "scope", nullable = false, columnDefinition = "TEXT")
    private String scope;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "start_date", nullable = false)
    private LocalDate start_date;

    @Column(name = "end_date", nullable = false)
    private LocalDate end_date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @ManyToMany
    @JoinTable(
        name = "audit_auditor",
        joinColumns = @JoinColumn(name = "audit_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"audit_id", "user_id"})
    )
    private Set<AuthUser> auditors = new HashSet<>();

    @OneToMany(mappedBy = "audit")
    private Set<Checklist> checklists = new HashSet<>();

    @OneToMany(mappedBy = "audit")
    private Set<Instance> instances = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "audit_document",
            joinColumns = @JoinColumn(name = "audit_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"audit_id", "document_id"})
    )
    private Set<Document> documents = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private AuthUser createdBy;
}
