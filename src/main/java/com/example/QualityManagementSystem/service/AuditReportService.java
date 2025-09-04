package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.repository.AuditReportRepository;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditReportService {

    private final AuditRepository auditRepository;
    private final AuditReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AuditReportService(AuditRepository auditRepository,
                              AuditReportRepository reportRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.auditRepository = auditRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public AuditReport submitReport(Long auditId, Long submittedByUserId, String content, Long reviewerUserId) {
        Audit audit = auditRepository.findById(auditId).orElseThrow(() -> new RuntimeException("Audit not found"));
        AuthUser submitter = userRepository.findById(submittedByUserId).orElseThrow(() -> new RuntimeException("User not found"));
        AuthUser reviewer = reviewerUserId == null ? null : userRepository.findById(reviewerUserId).orElse(null);

        AuditReport report = new AuditReport();
        report.setAudit(audit);
        report.setSubmittedBy(submitter);
        report.setContent(content);
        report.setApprovalStatus(ApprovalStatus.PENDING);
        report.setReviewer(reviewer);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        AuditReport saved = reportRepository.save(report);
        if (reviewer != null) {
            notificationService.notifyApprovalPending(reviewer, saved);
        }
        return saved;
    }

    @Transactional
    public AuditReport approveReport(Long reportId, Long reviewerUserId, String comments) {
        AuditReport report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        AuthUser reviewer = userRepository.findById(reviewerUserId).orElseThrow(() -> new RuntimeException("Reviewer not found"));
        report.setReviewer(reviewer);
        report.setReviewerComments(comments);
        report.setApprovalStatus(ApprovalStatus.APPROVED);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public AuditReport rejectReport(Long reportId, Long reviewerUserId, String comments) {
        AuditReport report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        AuthUser reviewer = userRepository.findById(reviewerUserId).orElseThrow(() -> new RuntimeException("Reviewer not found"));
        report.setReviewer(reviewer);
        report.setReviewerComments(comments);
        report.setApprovalStatus(ApprovalStatus.REJECTED);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }
}


