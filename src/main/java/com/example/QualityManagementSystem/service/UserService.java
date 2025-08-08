package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.RegisterRequest;
import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Role;
import com.example.QualityManagementSystem.repository.UserRepository;
import com.example.QualityManagementSystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
        
        // TODO: Send email with temp password and login link
        sendUserCreationEmail(savedUser, tempPassword);
        
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
        user.setActive(isActive);
        return userRepository.save(user);
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
        
        // TODO: Send password reset email
        sendPasswordResetEmail(user, tempPassword);
    }

    public boolean validateUserCredentials(String username, String password) {
        Optional<AuthUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            AuthUser user = userOpt.get();
            System.out.println("Password entered: " + password);
            System.out.println("Password hash in DB: " + user.getPassword());
            System.out.println("Password matches: " + passwordEncoder.matches(password, user.getPassword()));
            String hashedPassword = passwordEncoder.encode(password);
            System.out.println("BCrypt hash: " + hashedPassword);
            return user.isActive() && passwordEncoder.matches(password, user.getPassword());
        }

        return false;
    }

    public boolean isForcePasswordReset(String username) {
        Optional<AuthUser> userOpt = userRepository.findByUsername(username);
        return userOpt.map(AuthUser::isForcePasswordReset).orElse(false);
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
        // TODO: Implement email service
        // Email should contain:
        // - Temporary password
        // - Login URL
        // - Message: "You've been added to the Audit Management System by Admin. Please login and change your password."
        System.out.println("Sending user creation email to: " + user.getEmail());
        System.out.println("Temporary password: " + tempPassword);
        System.out.println("Login URL: /login");
    }

    private void sendPasswordResetEmail(AuthUser user, String tempPassword) {
        // TODO: Implement email service
        // Email should contain:
        // - New temporary password
        // - Login URL
        // - Message about password reset
        System.out.println("Sending password reset email to: " + user.getEmail());
        System.out.println("New temporary password: " + tempPassword);
        System.out.println("Login URL: /login");
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
}
