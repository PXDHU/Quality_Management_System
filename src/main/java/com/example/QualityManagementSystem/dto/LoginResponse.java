package com.example.QualityManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String message;
    private boolean forcePasswordReset;
    private String role;
    private String username;
    private String fullName;
} 