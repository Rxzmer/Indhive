package com.indhive.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Método para generar el token JWT
    public String generateJwtToken(String username, String roles) {
        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)  // Aquí agregamos los roles al token
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    // Obtener el username desde el token JWT
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    // Obtener los roles desde el token JWT
    public String getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.get("roles", String.class);  // Extraemos los roles
    }

    // Validar el token JWT
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            // Log si quieres: e.printStackTrace();
            return false;
        }
    }
}
