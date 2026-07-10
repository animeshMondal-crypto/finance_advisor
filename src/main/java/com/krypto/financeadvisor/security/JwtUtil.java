package com.krypto.financeadvisor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


//This is custom utility class
// that wraps JJWT.
// It has 3 jobs: generate a token, validate a token, extract the email from a token.

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long expirationMs;

    // Spring injects the values from application.yml into the constructor
    public JwtUtil (
            @Value("${app.jwt.secret}") String secret,
            @Value("{app.jwt.expiration-ms}") long expirationMs
    ){
        // Keys.hmacShaKeyFor converts your plain string secret into a
        // cryptographically safe SecretKey object suitable for HS256
        this.secretKey= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs=expirationMs;
    }

    // Called after successful login — returns the token string
    public String generateToken(String email){
        return Jwts.builder()
                .subject(email)               // who the token belongs to
                .issuedAt(new Date())        // when it was created
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)        // 0.12.x — algorithm inferred from key
                .compact();                 // produces the final token string
    }

    // Called in the filter on every request to confirm token is genuine and not expired
    public boolean validate(String token){
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Called in the filter to know WHICH user is making the request
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Internal helper — parses and verifies signature in one shot
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)       // 0.12.x replaces setSigningKey()
                .build()
                .parseSignedClaims(token)    // 0.12.x replaces parseClaimsJws()
                .getPayload();               // 0.12.x replaces getBody()
    }

}
