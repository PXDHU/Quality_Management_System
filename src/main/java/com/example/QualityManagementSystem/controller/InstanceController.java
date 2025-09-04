package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.InstanceCreateRequest;
import com.example.QualityManagementSystem.dto.InstanceResponse;
import com.example.QualityManagementSystem.service.InstanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instances")
@CrossOrigin(origins = "*")
public class InstanceController {

    private final InstanceService service;

    public InstanceController(InstanceService service) {
        this.service = service;
    }

    // Create an Instance for an Audit + Checklist Item
    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping
    public ResponseEntity<InstanceResponse> create(@RequestBody InstanceCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInstance(req));
    }
}
