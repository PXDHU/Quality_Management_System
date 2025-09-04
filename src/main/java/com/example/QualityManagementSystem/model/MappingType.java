package com.example.QualityManagementSystem.model;

public enum MappingType {
    EXACT_MATCH,      // Identical requirements
    HIGH_SIMILARITY,  // Very similar requirements with minor differences
    MEDIUM_SIMILARITY, // Somewhat similar requirements
    LOW_SIMILARITY,   // Minimal similarity
    RELATED,          // Related but not directly similar
    NO_MAPPING        // No meaningful relationship
}
