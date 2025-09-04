package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.RegisterRequest;
import com.example.QualityManagementSystem.dto.RegisterAdminRequest;
import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Role;
import com.example.QualityManagementSystem.repository.UserRepository;
import com.example.QualityManagementSystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    public AuthUser getUserById(Long userId){
        return userRepository.findById(userId).orElseThrow(()-> new RuntimeException("USER NOT FOUND"));
    }

    public AuthUser getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("USER NOT FOUND"));
    }

    public AuthUser createUserByAdmin(RegisterRequest registerRequest){
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("USERNAME ALREADY EXISTS");
        }
        
        String tempPassword = generateSecureTempPassword();
        
        AuthUser user = new AuthUser();
        user.setUsername(registerRequest.getUsername()); // username is email
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setEmail(registerRequest.getUsername()); // email same as username
        user.setFullName(registerRequest.getFullName());
        user.setRole(registerRequest.getRole());
        user.setActive(true);
        user.setForcePasswordReset(true);
        
        AuthUser savedUser = userRepository.save(user);
        
        // Print temporary password to terminal for development
        System.out.println("=== DEVELOPMENT MODE ===");
        System.out.println("New user created:");
        System.out.println("Username: " + savedUser.getUsername());
        System.out.println("Full Name: " + savedUser.getFullName());
        System.out.println("Role: " + savedUser.getRole());
        System.out.println("Temporary Password: " + tempPassword);
        System.out.println("=========================");
        
        // Send email with temp password and login link
        try {
            sendUserCreationEmail(savedUser, tempPassword);
        } catch (Exception e) {
            // Log the email error but don't fail the user creation
            System.err.println("Failed to send user creation email: " + e.getMessage());
            // You could also log this to a proper logging framework
        }
        
        return savedUser;
    }

    public List<AuthUser> getAllUsers() {
        return userRepository.findAll();
    }

    public List<AuthUser> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<AuthUser> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    public AuthUser updateUserStatus(Long userId, boolean isActive) {
        AuthUser user = getUserById(userId);
        
        // Debug logging
        System.out.println("=== UPDATE USER STATUS DEBUG ===");
        System.out.println("User ID: " + userId);
        System.out.println("Username: " + user.getUsername());
        System.out.println("Current Status: " + user.isActive());
        System.out.println("New Status: " + isActive);
        
        user.setActive(isActive);
        AuthUser savedUser = userRepository.save(user);
        
        System.out.println("Saved Status: " + savedUser.isActive());
        System.out.println("================================");
        
        return savedUser;
    }

    public void deleteUser(Long userId) {
        AuthUser user = getUserById(userId);
        
        // Check if user is trying to delete themselves
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName().equals(user.getUsername())) {
            throw new RuntimeException("Cannot delete your own account");
        }
        
        // Check if user is the last admin
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.findByRole(Role.ADMIN).size();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last admin user");
            }
        }
        
        userRepository.delete(user);
    }

    public AuthUser updateUserProfile(Long userId, String fullName, String oldPassword, String newPassword) {
        AuthUser user = getUserById(userId);
        
        // Validate old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("INVALID OLD PASSWORD");
        }
        
        // Update full name
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        
        // Update password
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setForcePasswordReset(false);
        }
        
        return userRepository.save(user);
    }

    public void resetUserPassword(Long userId) {
        AuthUser user = getUserById(userId);
        String tempPassword = generateSecureTempPassword();
        
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setForcePasswordReset(true);
        
        userRepository.save(user);
        
        // Print temporary password to terminal for development
        System.out.println("=== DEVELOPMENT MODE ===");
        System.out.println("Password reset for user:");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Full Name: " + user.getFullName());
        System.out.println("New Temporary Password: " + tempPassword);
        System.out.println("=========================");
        
        // Send password reset email
        try {
            sendPasswordResetEmail(user, tempPassword);
        } catch (Exception e) {
            // Log the email error but don't fail the password reset
            System.err.println("Failed to send password reset email: " + e.getMessage());
            // You could also log this to a proper logging framework
        }
    }

    public AuthUser changePasswordDuringReset(String username, String newPassword) {
        AuthUser user = getUserByUsername(username);
        
        // Update password and clear force reset flag
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForcePasswordReset(false);
        
        return userRepository.save(user);
    }

    public boolean validateUserCredentials(String username, String password) {
        Optional<AuthUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            AuthUser user = userOpt.get();
            boolean isActive = user.isActive();

            // Debug logging
            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("Username: " + username);
            System.out.println("User Active: " + isActive);
            System.out.println("Stored Password Hash: " + user.getPassword());
            System.out.println("Input Password: " + password);
            System.out.println("Password Hash Length: " + (user.getPassword() != null ? user.getPassword().length() : "null"));
            System.out.println("=========================");

            // Primary: bcrypt match
            if (isActive && passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("BCrypt match successful!");
                return true;
            } else {
                System.out.println("BCrypt match failed!");
            }

            // Fallback migration: if DB stored plaintext and equals input, migrate to bcrypt
            String stored = user.getPassword();
            boolean looksHashed = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
            System.out.println("Password looks hashed: " + looksHashed);
            
            if (isActive && !looksHashed && stored != null && stored.equals(password)) {
                System.out.println("Plaintext match successful, migrating to BCrypt");
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                return true;
            }
        } else {
            System.out.println("User not found: " + username);
        }
        return false;
    }

    public boolean isForcePasswordReset(String username) {
        Optional<AuthUser> userOpt = userRepository.findByUsername(username);
        boolean forceReset = userOpt.map(AuthUser::isForcePasswordReset).orElse(false);
        System.out.println("=== FORCE PASSWORD RESET CHECK ===");
        System.out.println("Username: " + username);
        System.out.println("User exists: " + userOpt.isPresent());
        if (userOpt.isPresent()) {
            System.out.println("User forcePasswordReset flag: " + userOpt.get().isForcePasswordReset());
        }
        System.out.println("Returning: " + forceReset);
        System.out.println("==================================");
        return forceReset;
    }

    public boolean isUserActive(String username) {
        Optional<AuthUser> userOpt = userRepository.findByUsername(username);
        return userOpt.map(AuthUser::isActive).orElse(false);
    }

    private String generateSecureTempPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(26))); // Uppercase
        password.append(TEMP_PASSWORD_CHARS.charAt(26 + random.nextInt(26))); // Lowercase
        password.append(TEMP_PASSWORD_CHARS.charAt(52 + random.nextInt(10))); // Digit
        password.append(TEMP_PASSWORD_CHARS.charAt(62 + random.nextInt(8))); // Special char
        
        // Fill the rest randomly
        for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }

    private void sendUserCreationEmail(AuthUser user, String tempPassword) {
        String subject = "Your QMS account has been created";
        String body = "Hello " + user.getFullName() + ",\n\n" +
                "You have been added to the Audit Management System.\n" +
                "Temporary Password: " + tempPassword + "\n" +
                "Login URL: /login\n\n" +
                "Please log in and change your password.";
        emailService.sendPlainText(user.getEmail(), subject, body);
    }

    private void sendPasswordResetEmail(AuthUser user, String tempPassword) {
        String subject = "Your QMS password has been reset";
        String body = "Hello " + user.getFullName() + ",\n\n" +
                "Your password has been reset.\n" +
                "Temporary Password: " + tempPassword + "\n" +
                "Login URL: /login\n\n" +
                "Please log in and change your password.";
        emailService.sendPlainText(user.getEmail(), subject, body);
    }

    // Legacy methods for backward compatibility
    public AuthUser RoleRegister(RegisterRequest registerRequest){
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("USERNAME ALREADY EXISTS");
        }
        
        // For legacy compatibility, generate a temporary password
        String tempPassword = generateSecureTempPassword();
        
        AuthUser user = new AuthUser();
        user.setUsername(registerRequest.getUsername()); // username is email
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setEmail(registerRequest.getUsername()); // email same as username
        user.setFullName(registerRequest.getFullName());
        user.setRole(registerRequest.getRole());
        user.setActive(true);
        user.setForcePasswordReset(true); // Force password reset for legacy users
        return userRepository.save(user);
    }

    public AuthUser AdminRegister(RegisterRequest registerRequest){
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("USERNAME ALREADY EXISTS");
        }
        
        // For legacy compatibility, generate a temporary password
        String tempPassword = generateSecureTempPassword();
        
        AuthUser user = new AuthUser();
        user.setUsername(registerRequest.getUsername()); // username is email
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setEmail(registerRequest.getUsername()); // email same as username
        user.setFullName(registerRequest.getFullName());
        user.setRole(Role.ADMIN);
        user.setActive(true);
        user.setForcePasswordReset(true); // Force password reset for legacy users
        return userRepository.save(user);
    }

    // Check if any admin user exists
    public boolean hasAnyAdmin() {
        return userRepository.findByRole(Role.ADMIN).size() > 0;
    }

    // Create the first admin user
    public AuthUser createFirstAdmin(RegisterAdminRequest registerAdminRequest) {
        if (userRepository.findByUsername(registerAdminRequest.getUsername()).isPresent()) {
            throw new RuntimeException("USERNAME ALREADY EXISTS");
        }
        
        // Debug logging for admin creation
        System.out.println("=== ADMIN CREATION DEBUG ===");
        System.out.println("Username: " + registerAdminRequest.getUsername());
        System.out.println("Raw Password: " + registerAdminRequest.getPassword());
        System.out.println("Password Length: " + registerAdminRequest.getPassword().length());
        
        String encodedPassword = passwordEncoder.encode(registerAdminRequest.getPassword());
        System.out.println("Encoded Password: " + encodedPassword);
        System.out.println("Encoded Length: " + encodedPassword.length());
        System.out.println("=============================");
        
        AuthUser user = new AuthUser();
        user.setUsername(registerAdminRequest.getUsername()); // username is email
        user.setPassword(encodedPassword);
        user.setEmail(registerAdminRequest.getUsername()); // email same as username
        user.setFullName(registerAdminRequest.getFullName());
        user.setRole(Role.ADMIN);
        user.setActive(true);
        user.setForcePasswordReset(false); // No force reset for first admin
        
        return userRepository.save(user);
    }
}