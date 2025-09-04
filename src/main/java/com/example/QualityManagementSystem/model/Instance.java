package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_clause_instance")
public class Instance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instance_id")
    private Long instanceId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "audit_id", referencedColumnName = "audit_id")
    private Audit audit;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Checklist_item checklistItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clause_id", referencedColumnName = "clause_id")
    private Clause_library clause;

    @Enumerated(EnumType.STRING)
    @Column(name = "conformity_status", nullable = false)
    private ConformityStatus conformityStatus;

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
    private String validationComments;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance that = (Instance) o;
        return instanceId != null && instanceId.equals(that.getInstanceId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
