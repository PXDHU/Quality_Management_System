package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id", referencedColumnName = "audit_id")
    private Audit audit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", referencedColumnName = "user_id")
    private AuthUser submittedBy;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // could be summary or rich text; PDFs are generated separately

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", referencedColumnName = "user_id")
    private AuthUser reviewer;

    @Column(name = "reviewer_comments", columnDefinition = "TEXT")
    private String reviewerComments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditReport that = (AuditReport) o;
        return reportId != null && reportId.equals(that.getReportId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


