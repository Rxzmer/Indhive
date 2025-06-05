package com.indhive.controller;

import com.indhive.dto.UserDTO;
import com.indhive.dto.UserRequestDTO;
import com.indhive.model.User;
import com.indhive.service.UserService;
import com.indhive.security.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Listar todos los usuarios", description = "Devuelve la lista completa de usuarios. Solo accesible para usuarios con rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserDTO> listar() {
        return userService.listarUsuarios().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Obtener usuario por ID", description = "Devuelve la información de un usuario específico.")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> obtenerPorId(@PathVariable Long id) {
        return userService.obtenerUsuarioPorId(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un nuevo usuario", description = "Solo accesible para usuarios con rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDTO> crear(@Valid @RequestBody UserRequestDTO dto) {
        User nuevo = new User();
        nuevo.setUsername(dto.getUsername());
        nuevo.setEmail(dto.getEmail());
        nuevo.setPassword(dto.getPassword());
        nuevo.setRoles(dto.getRoles() != null ? dto.getRoles() : "ROLE_USER");

        User saved = userService.guardarUsuario(nuevo);
        return ResponseEntity.ok(toDTO(saved));
    }

    @Operation(summary = "Actualizar un usuario existente", description = "Solo accesible para admins o el propio usuario.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UserRequestDTO dto,
            Authentication auth) {
        String emailAutenticado = auth.getName();

        Optional<User> userOpt = userService.obtenerUsuarioPorId(id);
        if (userOpt.isEmpty())
            return ResponseEntity.notFound().build();

        User u = userOpt.get();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSelf = u.getEmail().equals(emailAutenticado);

        if (!isAdmin && !isSelf)
            return ResponseEntity.status(403).build();

        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        if (isAdmin && dto.getRoles() != null) {
            u.setRoles(dto.getRoles());
        }

        User actualizado = userService.actualizarUsuario(u.getId(), u);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @Operation(summary = "Eliminar un usuario", description = "Solo accesible para usuarios con rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (userService.obtenerUsuarioPorId(id).isPresent()) {
            userService.eliminarUsuario(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Listar proyectos de un usuario", description = "Proyectos propios y colaboraciones.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/projects")
    public ResponseEntity<Map<String, Object>> listarProyectosDeUsuario(@PathVariable Long id) {
        return userService.obtenerUsuarioPorId(id)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("ownedProjects", user.getOwnedProjects());
                    response.put("collaboratedProjects", user.getCollaboratedProjects());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cambiar mi contraseña", description = "Requiere autenticación.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body, Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.obtenerUsuarioPorEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.status(404).body("Usuario no encontrado");

        String nuevaPassword = body.get("password");
        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Contraseña no puede estar vacía");
        }

        User user = userOpt.get();
        user.setPassword(nuevaPassword);
        userService.guardarUsuario(user);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @Operation(summary = "Actualizar mi perfil", description = "Actualiza nombre y email del usuario autenticado.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<Map<String, String>> actualizarMiPerfil(@Valid @RequestBody UserRequestDTO dto,
            Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.obtenerUsuarioPorEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.status(404).build();

        User user = userOpt.get();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        User actualizado = userService.actualizarUsuario(user.getId(), user);
        String nuevoToken = jwtUtils.generateJwtToken(actualizado.getEmail(), actualizado.getRoles());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Perfil actualizado correctamente");
        response.put("token", nuevoToken);
        return ResponseEntity.ok(response);
    }

    // Conversión manual User → UserDTO
    private UserDTO toDTO(User u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getEmail(), u.getRoles());
    }

    @Operation(summary = "Conviértete en creador", description = "Añade el rol CREATOR al usuario autenticado.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/creator")
    public ResponseEntity<Map<String, String>> convertirseEnCreador(Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.obtenerUsuarioPorEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.status(404).build();

        User user = userOpt.get();
        String rolesActuales = user.getRoles();

        if (!rolesActuales.contains("ROLE_CREATOR")) {
            String nuevosRoles = rolesActuales + (rolesActuales.isBlank() ? "" : ",") + "ROLE_CREATOR";
            user.setRoles(nuevosRoles);
            userService.guardarUsuario(user);
        }

        String nuevoToken = jwtUtils.generateJwtToken(user.getEmail(), user.getRoles());

        return ResponseEntity.ok(Map.of("token", nuevoToken));
    }
}
