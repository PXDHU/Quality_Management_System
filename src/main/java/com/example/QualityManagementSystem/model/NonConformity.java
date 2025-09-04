package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "non_conformity")
public class NonConformity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "non_conformity_id")
    private Long nonConformityId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id", referencedColumnName = "audit_id")
    private Audit audit;

    // Optional pointer to the instance (clause evaluation) that raised the NC
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", referencedColumnName = "instance_id")
    private Instance instance;

    // Optional direct pointer to clause if you raise NC against a clause without an Instance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clause_id", referencedColumnName = "clause_id")
    private Clause_library clause;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AuthUser createdBy;

    // Auditee / responsible team
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private AuthUser assignedTo;

    @ElementCollection
    @CollectionTable(name = "nc_evidence", joinColumns = @JoinColumn(name = "non_conformity_id"))
    @Column(name = "evidence_id")
    private List<String> evidenceIds = new ArrayList<>();

    @OneToMany(mappedBy = "nc", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CorrectiveAction> actions = new HashSet<>();

    @OneToMany(mappedBy = "nc", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<RCAStep> rcaSteps = new ArrayList<>();

    // Many NCs ↔ Many Documents (for evidence)
    @ManyToMany
    @JoinTable(
            name = "document_nc",
            joinColumns = @JoinColumn(name = "non_conformity_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"non_conformity_id", "document_id"})
    )
    private Set<Document> evidenceDocuments = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonConformity that = (NonConformity) o;
        return nonConformityId != null && nonConformityId.equals(that.getNonConformityId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
