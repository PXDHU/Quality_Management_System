package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.NotificationRule;
import com.example.QualityManagementSystem.model.NotificationEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRuleRepository extends JpaRepository<NotificationRule, Long> {
    List<NotificationRule> findByEventTypeAndActiveTrue(NotificationEventType eventType);
}


