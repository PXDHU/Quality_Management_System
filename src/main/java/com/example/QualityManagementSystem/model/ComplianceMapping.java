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
@Table(name = "compliance_mapping")
public class ComplianceMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_clause_id", nullable = false)
    private Clause_library sourceClause;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_clause_id", nullable = false)
    private Clause_library targetClause;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_type", nullable = false)
    private MappingType mappingType;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "mapping_notes", columnDefinition = "TEXT")
    private String mappingNotes;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private AuthUser verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplianceMapping that = (ComplianceMapping) o;
        return mappingId != null && mappingId.equals(that.getMappingId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
