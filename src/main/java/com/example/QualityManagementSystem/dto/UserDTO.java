package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private Role role;
    private boolean active;  // Changed from isActive to active
}





