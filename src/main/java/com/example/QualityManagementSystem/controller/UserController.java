package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Role;
import com.example.QualityManagementSystem.repository.UserRepository;
import com.example.QualityManagementSystem.service.UserService;
import com.example.QualityManagementSystem.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // 0. POST /api/auth/register-admin - Register the first admin (no authentication required)
    @PostMapping("/auth/register-admin")
    public ResponseEntity<Map<String, String>> registerAdmin(@RequestBody RegisterAdminRequest registerAdminRequest) {
        try {
            // Check if any admin already exists
            if (userService.hasAnyAdmin()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Admin already exists. Only one admin can be registered.");
                return ResponseEntity.badRequest().body(response);
            }
            
            userService.createFirstAdmin(registerAdminRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin registered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/debug-auth")
    public ResponseEntity<?> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(auth.getAuthorities());
    }

    @GetMapping("/debug-user/{username}")
    public ResponseEntity<?> debugUser(@PathVariable String username) {
        try {
            AuthUser user = userService.getUserByUsername(username);
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("userId", user.getUserId());
            debugInfo.put("username", user.getUsername());
            debugInfo.put("fullName", user.getFullName());
            debugInfo.put("role", user.getRole());
            debugInfo.put("isActive", user.isActive());
            debugInfo.put("passwordHash", user.getPassword());
            debugInfo.put("passwordHashLength", user.getPassword() != null ? user.getPassword().length() : 0);
            debugInfo.put("forcePasswordReset", user.isForcePasswordReset());
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/debug-password")
    public ResponseEntity<?> debugPassword(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            String hash = request.get("hash");
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("inputPassword", password);
            debugInfo.put("storedHash", hash);
            debugInfo.put("hashLength", hash != null ? hash.length() : 0);
            
            // Test BCrypt matching
            if (hash != null && password != null) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                boolean matches = encoder.matches(password, hash);
                debugInfo.put("bcryptMatches", matches);
                
                // Generate new hash for comparison
                String newHash = encoder.encode(password);
                debugInfo.put("newHash", newHash);
                debugInfo.put("newHashLength", newHash.length());
                debugInfo.put("newHashMatches", encoder.matches(password, newHash));
            }
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/debug-activate-user/{username}")
    public ResponseEntity<?> debugActivateUser(@PathVariable String username) {
        try {
            AuthUser user = userService.getUserByUsername(username);
            user.setActive(true);
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User activated successfully");
            response.put("username", username);
            response.put("isActive", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug-user-status/{username}")
    public ResponseEntity<?> debugUserStatus(@PathVariable String username) {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("username", username);
            status.put("exists", userService.getUserByUsername(username) != null);
            status.put("isActive", userService.isUserActive(username));
            status.put("forcePasswordReset", userService.isForcePasswordReset(username));
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug-user-db/{userId}")
    public ResponseEntity<?> debugUserFromDB(@PathVariable Long userId) {
        try {
            AuthUser user = userService.getUserById(userId);
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("userId", user.getUserId());
            debugInfo.put("username", user.getUsername());
            debugInfo.put("fullName", user.getFullName());
            debugInfo.put("role", user.getRole());
            debugInfo.put("isActive", user.isActive());
            debugInfo.put("isActiveMethod", user.isActive());
            debugInfo.put("activeField", user.isActive());
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test-dto")
    public ResponseEntity<?> testDTO() {
        try {
            // Test creating a DTO with different values
            UserDTO testDto1 = new UserDTO(1, "test1", "Test User 1", Role.ADMIN, true);
            UserDTO testDto2 = new UserDTO(2, "test2", "Test User 2", Role.AUDITOR, false);
            
            Map<String, Object> result = new HashMap<>();
            result.put("testDto1", testDto1);
            result.put("testDto2", testDto2);
            result.put("testDto1Active", testDto1.isActive());
            result.put("testDto2Active", testDto2.isActive());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 1. POST /api/admin/users - Admin creates a new user with role assignment
    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest registerRequest) {
        try {
            AuthUser createdUser = userService.createUserByAdmin(registerRequest);
            
            // Return the created user data along with success message
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully and credentials sent via email");
            response.put("user", new UserDTO(
                createdUser.getUserId(),
                createdUser.getUsername(),
                createdUser.getFullName(),
                createdUser.getRole(),
                createdUser.isActive()
            ));
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
    public ResponseEntity<List<UserDTO>> getAllUsers(
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

        List<UserDTO> dtos = users.stream().map(u -> new UserDTO(
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getRole(),
                u.isActive()
        )).toList();
        
        System.out.println("=== GET ALL USERS DEBUG ===");
        System.out.println("Total users found: " + users.size());
        dtos.forEach(dto -> {
            System.out.println("User: " + dto.getUsername() + ", Active: " + dto.isActive());
        });
        System.out.println("===========================");
        
        return ResponseEntity.ok(dtos);
    }

    // 3. PATCH /api/admin/users/{id}/status - Activate/Deactivate user
    @PatchMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusUpdateRequest request) {
        try {
            System.out.println("=== UPDATE USER STATUS DEBUG ===");
            System.out.println("User ID: " + id);
            System.out.println("Requested Status: " + request.isActive());
            System.out.println("Request Body: " + request.toString());
            
            AuthUser updatedUser = userService.updateUserStatus(id, request.isActive());
            
            System.out.println("Updated User Status: " + updatedUser.isActive());
            System.out.println("Returning DTO with Status: " + updatedUser.isActive());
            
            UserDTO responseDto = new UserDTO(
                    updatedUser.getUserId(),
                    updatedUser.getUsername(),
                    updatedUser.getFullName(),
                    updatedUser.getRole(),
                    updatedUser.isActive()
            );
            
            System.out.println("Response DTO Status: " + responseDto.isActive());
            System.out.println("==================================");
            
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            System.err.println("Error updating user status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // 4. DELETE /api/admin/users/{id} - Delete user
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
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
                    System.out.println("User " + username + " needs password reset: " + true);
                } else {
                    response.setForcePasswordReset(false);
                    response.setMessage("Login successful");
                    System.out.println("User " + username + " needs password reset: " + false);
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

    // 7. POST /api/auth/change-password - User changes password during forced reset
    @PostMapping("/auth/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String newPassword = request.get("newPassword");
            
            if (username == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and new password are required"));
            }
            
            userService.changePasswordDuringReset(username, newPassword);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully. Please login with your new password.");
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
