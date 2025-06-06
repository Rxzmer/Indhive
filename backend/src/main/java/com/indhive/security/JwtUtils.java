package com.indhive.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("La clave secreta JWT debe tener al menos 32 caracteres.");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(String email, String roles) {
        String rolesWithPrefix = normalizeRoles(roles);

        return Jwts.builder()
            .setSubject(email) // ahora usamos el email como identificador
            .claim("roles", rolesWithPrefix)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + Duration.ofHours(24).toMillis()))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }  

    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRolesFromJwtToken(String token) {
        return parseClaims(token).get("roles", String.class);
    }

    public Optional<String> safeGetUserName(String token) {
        try {
            return Optional.of(getUserNameFromJwtToken(token));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parseClaims(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Token no soportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Token mal formado: " + e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            System.out.println("Token inválido: " + e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String normalizeRoles(String roles) {
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.joining(","));
    }
}
