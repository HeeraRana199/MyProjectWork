package com.cts.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtils - Utility class for generating and validating JWT tokens.
 *
 * JWT = JSON Web Token. Used for stateless authentication.
 * Flow: User logs in → gets a token → sends token in every request header.
 * Server validates the token on each request (no session storage needed).
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /** Secret key from application.properties */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Token validity duration in milliseconds (default: 24 hours) */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generate a signed JWT token for a successfully authenticated user.
     *
     * @param username the authenticated user's username (email)
     * @return signed JWT token string
     */
    public String generateJwtToken(String username) {
        // Build HMAC-SHA key from the secret string
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)                                  // payload: who is this token for
                .setIssuedAt(new Date())                               // when was it issued
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // expiry
                .signWith(key, SignatureAlgorithm.HS256)               // sign with HMAC-SHA256
                .compact();
    }

    /**
     * Extract username (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return username embedded in the token
     */
    public String getUsernameFromJwtToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validate the JWT token - check signature and expiry.
     *
     * @param authToken the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
 