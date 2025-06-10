package com.indhive.controller;

import com.indhive.dto.EmailDTO;
import com.indhive.dto.LoginRequest;
import com.indhive.dto.ResetPasswordDTO;
import com.indhive.dto.UserDTO;
import com.indhive.model.RevokedToken;
import com.indhive.model.User;
import com.indhive.repository.RevokedTokenRepository;
import com.indhive.repository.UserRepository;
import com.indhive.security.JwtUtils;
import com.indhive.security.LoginAttemptService;
import com.indhive.service.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "API para registro, login y obtención de datos del usuario autenticado")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private RevokedTokenRepository revokedTokenRepository;
    @Autowired private LoginAttemptService loginAttemptService;
    @Autowired private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username ya está en uso");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Error: El correo ya está en uso");
        }

        if (user.getRoles() == null || user.getRoles().isBlank()) {
            user.setRoles("ROLE_USER");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        savedUser.setPassword(null); // limpiar hash antes de devolver

        return ResponseEntity.ok(new UserDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRoles()));
    }

    @PostMapping("/recover")
    public ResponseEntity<?> recoverPassword(@RequestBody EmailDTO emailDTO) {
        Optional<User> userOpt = userRepository.findByEmail(emailDTO.getEmail());
        if (userOpt.isPresent()) {
            String token = jwtUtils.generateJwtToken(userOpt.get().getEmail(), normalizeRoles(userOpt.get().getRoles()));
            String recoveryUrl = "http://localhost:3000/reset-password?token=" + token;

            emailService.sendSimpleMessage(
                emailDTO.getEmail(),
                "Recuperación de contraseña - Indhive",
                "Haz clic en este enlace para restablecer tu contraseña:\n\n" + recoveryUrl
            );
        }

        return ResponseEntity.ok("Si el correo está registrado, recibirás instrucciones.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        try {
            String email = jwtUtils.getUserNameFromJwtToken(dto.getToken());
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(user);

            if (!revokedTokenRepository.existsById(dto.getToken())) {
                revokedTokenRepository.save(new RevokedToken(dto.getToken(), new Date()));
            }

            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Token inválido o expirado");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        if (loginAttemptService.isBlocked(email)) {
            return ResponseEntity.status(429).body("Demasiados intentos fallidos. Intenta más tarde.");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            loginAttemptService.loginSucceeded(email);

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401).body("Usuario no encontrado");
            }

            String normalizedRoles = normalizeRoles(userOpt.get().getRoles());
            String token = jwtUtils.generateJwtToken(email, normalizedRoles);

            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(email);
            return ResponseEntity.status(401).body("Error: Credenciales inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        String email = auth.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        return ResponseEntity.ok(new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRoles()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!revokedTokenRepository.existsById(token)) {
                revokedTokenRepository.save(new RevokedToken(token, new Date()));
            }
            return ResponseEntity.ok("Token revocado exitosamente");
        }
        return ResponseEntity.badRequest().body("Token no proporcionado");
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        String email = auth.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        String normalizedRoles = normalizeRoles(userOpt.get().getRoles());
        String newToken = jwtUtils.generateJwtToken(email, normalizedRoles);

        return ResponseEntity.ok(Map.of("token", newToken));
    }

    private String normalizeRoles(String rawRoles) {
        return Arrays.stream(rawRoles.split(","))
            .map(String::trim)
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .collect(Collectors.joining(","));
    }
}
