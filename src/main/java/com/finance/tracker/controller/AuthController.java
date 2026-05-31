package com.finance.tracker.controller;

import com.finance.tracker.domain.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> loginRequest) {
        String username = (String) loginRequest.get("username");
        String password = (String) loginRequest.get("password");
        Boolean rememberMe = loginRequest.containsKey("rememberMe") ? (Boolean) loginRequest.get("rememberMe") : false;

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token;
            if (rememberMe) {
                token = jwtUtil.generateRememberMeToken(user.getUsername());
                user.setRememberMeEnabled(true);
            } else {
                token = jwtUtil.generateToken(user.getUsername());
                user.setRememberMeEnabled(false);
            }
            userRepository.save(user);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("rememberMe", rememberMe);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("resetToken", resetToken);
        response.put("message", "Password reset token generated. Check email for instructions. (For demo: copy the resetToken)");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> resetToken.equals(u.getPasswordResetToken()))
                .findFirst();
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid reset token");
        }
        User user = userOpt.get();
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Reset token has expired");
        }
        return ResponseEntity.ok("Reset token is valid");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");
        
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> resetToken.equals(u.getPasswordResetToken()))
                .findFirst();
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid reset token");
        }
        User user = userOpt.get();
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        
        return ResponseEntity.ok("Password reset successfully");
    }
}