package com.example.QualityManagementSystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"auditors", "checklists", "instances", "documents"})
@Table(name = "audit")
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "scope", nullable = false, columnDefinition = "TEXT")
    private String scope;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    // Enhanced fields for audit planning
    @Column(name = "audit_type")
    private String auditType; // INTERNAL, EXTERNAL, SUPPLIER, etc.

    @Column(name = "department")
    private String department; // Department being audited

    @Column(name = "location")
    private String location; // Location of the audit

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Additional notes for the audit

    @Column(name = "current_phase")
    private String currentPhase; // PLANNING, EXECUTION, REPORTING, FOLLOW_UP

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @ManyToMany
    @JoinTable(
            name = "audit_auditor",
            joinColumns = @JoinColumn(name = "audit_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"audit_id", "user_id"})
    )
    @JsonIgnore // ✅ Prevents Jackson from serializing auditors directly
    private Set<AuthUser> auditors = new HashSet<>();

    @OneToMany(mappedBy = "audit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Checklist> checklists = new HashSet<>();
    
    // Helper methods to manage bidirectional relationships
    public void addChecklist(Checklist checklist) {
        if (checklists == null) {
            checklists = new HashSet<>();
        }
        // Prevent infinite recursion by checking if already exists
        if (!checklists.contains(checklist)) {
            checklists.add(checklist);
            checklist.setAudit(this);
        }
    }
    
    public void removeChecklist(Checklist checklist) {
        if (checklists != null) {
            checklists.remove(checklist);
            if (checklist.getAudit() == this) {
                checklist.setAudit(null);
            }
        }
    }

    @OneToMany(mappedBy = "audit")
    @JsonIgnore
    private Set<Instance> instances = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "audit_document",
            joinColumns = @JoinColumn(name = "audit_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"audit_id", "document_id"})
    )
    @JsonIgnore
    private List<Document> documents = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private AuthUser createdBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audit that = (Audit) o;
        return auditId != null && auditId.equals(that.getAuditId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
