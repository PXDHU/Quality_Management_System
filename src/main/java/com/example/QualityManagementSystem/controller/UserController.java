package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Role;
import com.example.QualityManagementSystem.service.UserService;
import com.example.QualityManagementSystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/debug-auth")
    public ResponseEntity<?> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(auth.getAuthorities());
    }

    // 1. POST /api/admin/users - Admin creates a new user with role assignment
    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody RegisterRequest registerRequest) {
        try {
            userService.createUserByAdmin(registerRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User created successfully and credentials sent via email");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 2. GET /api/admin/users - List all users with filters
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuthUser>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive) {
        
        List<AuthUser> users;
        if (role != null) {
            users = userService.getUsersByRole(role);
        } else if (isActive != null && isActive) {
            users = userService.getActiveUsers();
        } else {
            users = userService.getAllUsers();
        }
        
        return ResponseEntity.ok(users);
    }

    // 3. PATCH /api/admin/users/{id}/status - Activate/Deactivate user
    @PatchMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthUser> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusUpdateRequest request) {
        try {
            AuthUser updatedUser = userService.updateUserStatus(id, request.isActive());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 4. PUT /api/users/{id}/profile - User updates their full name and password
    @PutMapping("/users/{id}/profile")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UserProfileUpdateRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthUser currentUser = userService.getUserByUsername(auth.getName());

            if (!currentUser.getUserId().equals(id.intValue())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own profile." );
            }

            AuthUser updatedUser = userService.updateUserProfile(
                    id,
                    request.getFullName(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            // Send back the error message for debugging
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            System.out.println("Attempting login for: " + username);

            if (userService.validateUserCredentials(username, password)) {
                AuthUser user = userService.getUserByUsername(username);
                System.out.println("Login successful for: " + username);

                String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setRole(user.getRole().name());
                response.setUsername(user.getUsername());
                response.setFullName(user.getFullName());

                if (userService.isForcePasswordReset(username)) {
                    response.setForcePasswordReset(true);
                    response.setMessage("Please change your password before proceeding");
                } else {
                    response.setForcePasswordReset(false);
                    response.setMessage("Login successful");
                }

                return ResponseEntity.ok(response);
            } else {
                System.out.println("Invalid credentials for: " + username);
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


    // 6. POST /api/auth/reset-password - Admin-initiated password reset for users
    @PostMapping("/auth/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            userService.resetUserPassword(request.getUserId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully and new credentials sent via email");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 7. GET /api/roles - Get all available user roles
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role[]> getRoles() {
        return ResponseEntity.ok(Role.values());
    }

    // Additional endpoint for getting current user info
    @GetMapping("/users/me")
    public ResponseEntity<AuthUser> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthUser user = userService.getUserByUsername(auth.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
