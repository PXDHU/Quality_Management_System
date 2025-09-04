package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.model.CorrectiveAction;
import com.example.QualityManagementSystem.model.NotificationEventType;
import com.example.QualityManagementSystem.model.NotificationRule;
import com.example.QualityManagementSystem.model.Status;
import com.example.QualityManagementSystem.repository.CorrectiveActionRepository;
import com.example.QualityManagementSystem.repository.NotificationRuleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ReminderScheduler {

    private final CorrectiveActionRepository actionRepository;
    private final NotificationService notificationService;
    private final NotificationRuleRepository ruleRepository;

    public ReminderScheduler(CorrectiveActionRepository actionRepository,
                             NotificationService notificationService,
                             NotificationRuleRepository ruleRepository) {
        this.actionRepository = actionRepository;
        this.notificationService = notificationService;
        this.ruleRepository = ruleRepository;
    }

    // Run every day at 08:00 server time
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDueSoonReminders() {
        List<CorrectiveAction> actions = actionRepository.findAll();
        List<NotificationRule> rules = ruleRepository.findByEventTypeAndActiveTrue(NotificationEventType.DEADLINE_APPROACHING);
        Set<Integer> daysBeforeSet = new HashSet<>();
        for (NotificationRule rule : rules) {
            daysBeforeSet.add(Math.max(0, rule.getDaysBefore()));
        }
        if (daysBeforeSet.isEmpty()) {
            daysBeforeSet.add(3); // default fallback
        }

        LocalDate today = LocalDate.now();
        for (CorrectiveAction action : actions) {
            if (action.getStatus() == Status.COMPLETED || action.getDueDate() == null) continue;
            LocalDate due = action.getDueDate();
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, due);
            if (daysBeforeSet.contains((int) daysUntilDue)) {
                notificationService.notifyDeadlineApproaching(action.getResponsible(), action);
            }
        }
    }
}


