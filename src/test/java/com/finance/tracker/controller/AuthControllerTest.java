package com.finance.tracker.controller;

import com.finance.tracker.domain.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldChangePasswordWhenCurrentPasswordMatches() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice", null));

        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded-old-password");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("encoded-new-password");

        ResponseEntity<?> response = authController.changePassword(Map.of(
                "currentPassword", "oldPassword123",
                "newPassword", "newPassword456"
        ));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("encoded-new-password", user.getPassword());
        assertNotNull(response.getBody());
    }
}
