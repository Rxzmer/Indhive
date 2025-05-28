package com.indhive.controller;

import com.indhive.model.User;
import com.indhive.repository.UserRepository;
import com.indhive.security.JwtUtils;
import com.indhive.dto.LoginRequest;

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

import java.util.Map;
import java.util.Optional;

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

    @Operation(summary = "Registrar nuevo usuario",
               description = "Registra un usuario con username, email, password y roles. Devuelve el usuario creado sin la contraseña.")
    @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400", description = "Username ya está en uso")
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

    @Operation(summary = "Login de usuario",
               description = "Autentica usuario y genera un token JWT para acceder a recursos protegidos.")
    @ApiResponse(responseCode = "200", description = "Login exitoso, token JWT generado",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(example = "{\"token\":\"eyJhbGciOiJIUzUxMiJ9...\"}")))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userRepository.findByUsername(username).orElseThrow();

            String token = jwtUtils.generateJwtToken(user.getUsername(), user.getRoles());

            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Error: Credenciales inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener datos del usuario autenticado",
               description = "Devuelve información del usuario actualmente autenticado, excepto la contraseña.")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado encontrado",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = User.class)))
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
}
