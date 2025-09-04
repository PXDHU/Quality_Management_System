package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.repository.NotificationRuleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private final EmailService emailService;
    private final NotificationRuleRepository ruleRepository;

    public NotificationService(EmailService emailService, NotificationRuleRepository ruleRepository) {
        this.emailService = emailService;
        this.ruleRepository = ruleRepository;
    }

    public void notifyTaskAssigned(AuthUser assignee, CorrectiveAction action, NonConformity nc) {
        List<NotificationRule> rules = ruleRepository.findByEventTypeAndActiveTrue(NotificationEventType.TASK_ASSIGNED);
        if (assignee == null || assignee.getEmail() == null) return;
        String subject = "New Corrective Action Assigned";
        String body = "You have been assigned a corrective action for NC: " + nc.getTitle() +
                "\nDescription: " + action.getDescription() +
                (action.getDueDate() != null ? "\nDue Date: " + action.getDueDate() : "") +
                "\nPlease log in to the QMS to review.";
        emailService.sendPlainText(assignee.getEmail(), subject, body);
    }

    public void notifyApprovalPending(AuthUser reviewer, AuditReport report) {
        if (reviewer == null || reviewer.getEmail() == null) return;
        String subject = "Audit Report Pending Approval";
        String body = "An audit report is awaiting your review for audit ID: " + report.getAudit().getAuditId() +
                "\nPlease log in to approve or reject.";
        emailService.sendPlainText(reviewer.getEmail(), subject, body);
    }

    public void notifyDeadlineApproaching(AuthUser user, CorrectiveAction action) {
        List<NotificationRule> rules = ruleRepository.findByEventTypeAndActiveTrue(NotificationEventType.DEADLINE_APPROACHING);
        if (user == null || user.getEmail() == null) return;
        String subject = "Corrective Action Due Soon";
        String due = action.getDueDate() != null ? action.getDueDate().format(DateTimeFormatter.ISO_DATE) : "(no due date)";
        String body = "Your corrective action '" + action.getDescription() + "' is due on " + due + ".";
        emailService.sendPlainText(user.getEmail(), subject, body);
    }
}


