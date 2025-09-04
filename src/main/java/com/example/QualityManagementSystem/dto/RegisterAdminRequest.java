package com.example.QualityManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterAdminRequest {
    private String username; // email address
    private String fullName;
    private String password; // required for first admin
}
