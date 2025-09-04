package com.example.QualityManagementSystem.model;

public enum ConformityStatus {
    COMPLIANT,           // Clause meets all requirements
    NON_COMPLIANT,       // Clause fails to meet requirements
    PARTIALLY_COMPLIANT, // Clause meets some requirements but not all
    NOT_APPLICABLE,      // Clause doesn't apply to this audit
    PENDING_EVALUATION   // Clause hasn't been evaluated yet
}
