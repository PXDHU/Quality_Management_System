package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.model.NotificationRule;
import com.example.QualityManagementSystem.model.NotificationEventType;
import com.example.QualityManagementSystem.repository.NotificationRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/rules")
@CrossOrigin(origins = "*")
public class NotificationRuleController {

    private final NotificationRuleRepository repository;

    public NotificationRuleController(NotificationRuleRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NotificationRule> create(@RequestBody NotificationRule rule) {
        return ResponseEntity.ok(repository.save(rule));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<NotificationRule>> list() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<NotificationRule> update(@PathVariable Long id, @RequestBody NotificationRule rule) {
        rule.setRuleId(id);
        return ResponseEntity.ok(repository.save(rule));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


