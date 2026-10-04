package com.finance.tracker.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    @Test
    void rememberMeTokenExpiresWithinThirtyDays() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyThatIsAtLeast256BitsLong");
        jwtUtil.init();
        long issuedAt = System.currentTimeMillis();

        String token = jwtUtil.generateRememberMeToken("alice");
        Date expiration = jwtUtil.extractClaim(token, Claims::getExpiration);

        assertEquals("alice", jwtUtil.extractUsername(token));
        assertTrue(expiration.getTime() > issuedAt);
        assertTrue(expiration.getTime() - issuedAt <= Duration.ofDays(30).toMillis());
    }
}