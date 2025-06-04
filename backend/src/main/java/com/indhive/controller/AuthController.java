package com.indhive.controller;

import com.indhive.model.RevokedToken;
import com.indhive.model.User;
import com.indhive.repository.UserRepository;
import com.indhive.repository.RevokedTokenRepository;
import com.indhive.security.JwtUtils;
import com.indhive.security.LoginAttemptService;
import com.indhive.dto.LoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.indhive.dto.ResetPasswordDTO;

import java.util.Map;
import java.util.Optional;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "API para registro, login y obtención de datos del usuario autenticado")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Operation(summary = "Registrar nuevo usuario", description = "Registra un usuario con username, email, password y roles. Devuelve el usuario creado sin la contraseña.")
    @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400", description = "Username ya está en uso")
    @PostMapping("/recover")
    public ResponseEntity<?> recoverPassword(@RequestBody EmailDTO emailDTO) {
        System.out.println(">> Solicitud recibida para recuperar: " + emailDTO.getEmail());

        Optional<User> userOpt = userRepository.findByEmail(emailDTO.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok("Si el correo está registrado, recibirás instrucciones.");
        }

        String token = jwtUtils.generateJwtToken(userOpt.get().getUsername(), userOpt.get().getRoles());

        System.out.println("Enlace de recuperación: http://localhost:3000/reset-password?token=" + token);

        return ResponseEntity.ok("Si el correo está registrado, recibirás instrucciones.");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username ya está en uso");
        }

        if (user.getRoles() == null || user.getRoles().isBlank()) {
            user.setRoles("ROLE_USER");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        savedUser.setPassword(null);

        return ResponseEntity.ok(savedUser);
    }

    @Operation(summary = "Restablecer contraseña", description = "Cambia la contraseña de un usuario usando un token de recuperación.")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente")
    @ApiResponse(responseCode = "400", description = "Token inválido o expirado")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        try {
            String username = jwtUtils.getUserNameFromJwtToken(dto.getToken());
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(user);

            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Token inválido o expirado");
        }
    }

    @Operation(summary = "Login de usuario", description = "Autentica usuario y genera un token JWT para acceder a recursos protegidos.")
    @ApiResponse(responseCode = "200", description = "Login exitoso, token JWT generado", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"token\":\"eyJhbGciOiJIUzUxMiJ9...\"}")))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        String inputEmail = loginRequest.getEmail(); // campo usado en frontend
        String password = loginRequest.getPassword();

        if (loginAttemptService.isBlocked(inputEmail)) {
            return ResponseEntity.status(429).body("Demasiados intentos fallidos. Intenta más tarde.");
        }

        try {
            // Buscar al usuario por email
            Optional<User> userOpt = userRepository.findByEmail(inputEmail);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401).body("Usuario no encontrado");
            }

            String username = userOpt.get().getUsername();

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(auth);
            loginAttemptService.loginSucceeded(inputEmail);

            String token = jwtUtils.generateJwtToken(username, userOpt.get().getRoles());

            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(inputEmail);
            return ResponseEntity.status(401).body("Error: Credenciales inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener datos del usuario autenticado", description = "Devuelve información del usuario actualmente autenticado, excepto la contraseña.")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        String username = auth.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Logout", description = "Revoca el token JWT actual.")
    @ApiResponse(responseCode = "200", description = "Token revocado exitosamente")
    @ApiResponse(responseCode = "400", description = "Token no proporcionado")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (!revokedTokenRepository.existsById(token)) {
                revokedTokenRepository.save(new RevokedToken(token, new Date()));
            }

            return ResponseEntity.ok("Token revocado exitosamente");
        } else {
            return ResponseEntity.badRequest().body("Token no proporcionado");
        }
    }
}
