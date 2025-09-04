package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private NotificationEventType eventType;

    @Column(name = "days_before", nullable = false)
    private int daysBefore = 3; // e.g., 3 days before due date

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationRule that = (NotificationRule) o;
        return ruleId != null && ruleId.equals(that.getRuleId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


