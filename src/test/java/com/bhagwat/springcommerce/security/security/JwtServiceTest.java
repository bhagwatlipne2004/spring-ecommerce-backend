package com.bhagwat.springcommerce.security.security;

import com.bhagwat.springcommerce.user.entity.Role;
import com.bhagwat.springcommerce.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    private static final String SECRET =
            "this-is-a-test-secret-key-that-is-long-enough-for-hs256";
    private static final long EXPIRATION = 60_000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.CUSTOMER);
    }

    @Test
    void generateTokenShouldCreateTokenWithCorrectClaims() {
        String token = jwtService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("test@example.com", claims.getSubject());
        assertEquals("CUSTOMER", claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractUserNameShouldReturnEmail() {
        String token = jwtService.generateToken(user);

        assertEquals(
                "test@example.com",
                jwtService.extractUserName(token)
        );
    }

    @Test
    void isTokenValidShouldReturnTrueForMatchingUser() {
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValidShouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(user);

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setRole(Role.CUSTOMER);

        assertFalse(jwtService.isTokenValid(token, anotherUser));
    }

    @Test
    void extractUserNameShouldThrowForMalformedToken() {
        assertThrows(
                RuntimeException.class,
                () -> jwtService.extractUserName("not-a-valid-jwt")
        );
    }

    @Test
    void extractUserNameShouldThrowForTokenSignedWithAnotherKey() {
        String token = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(
                        "another-test-secret-key-that-is-long-enough-for-hs256"
                                .getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(
                RuntimeException.class,
                () -> jwtService.extractUserName(token)
        );
    }

    @Test
    void extractUserNameShouldThrowForExpiredToken() {
        String token = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(getSigningKey())
                .compact();

        assertThrows(
                RuntimeException.class,
                () -> jwtService.extractUserName(token)
        );
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}