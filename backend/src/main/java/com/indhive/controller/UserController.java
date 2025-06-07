package com.indhive.controller;

import com.indhive.dto.UserDTO;
import com.indhive.dto.UserRequestDTO;
import com.indhive.model.User;
import com.indhive.security.JwtUtils;
import com.indhive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Operation(summary = "Listar todos los usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserDTO> listar() {
        return userService.listarUsuarios().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> obtenerPorId(@PathVariable Long id) {
        return userService.obtenerUsuarioPorId(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un nuevo usuario")
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

    @Operation(summary = "Actualizar un usuario")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> actualizar(@PathVariable Long id,
                                              @Valid @RequestBody UserRequestDTO dto,
                                              Authentication auth) {
        Optional<User> userOpt = userService.obtenerUsuarioPorId(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSelf = auth.getName().equalsIgnoreCase(user.getEmail());

        if (!isAdmin && !isSelf) return ResponseEntity.status(403).build();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(dto.getPassword());
        }
        if (isAdmin && dto.getRoles() != null) {
            user.setRoles(dto.getRoles());
        }

        User actualizado = userService.actualizarUsuario(user.getId(), user);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @Operation(summary = "Eliminar un usuario")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (userService.obtenerUsuarioPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.eliminarUsuario(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Listar proyectos de un usuario")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/projects")
    public ResponseEntity<Map<String, Object>> listarProyectosDeUsuario(@PathVariable Long id) {
        return userService.obtenerUsuarioPorId(id)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("ownedProjects", user.getOwnedProjects());
                    response.put("collaboratedProjects", userService.buscarColaboraciones(user.getId()));
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cambiar mi contraseña")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body, Authentication auth) {
        String nuevaPassword = body.get("password");
        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Contraseña no puede estar vacía");
        }

        Optional<User> userOpt = userService.obtenerUsuarioPorEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(404).body("Usuario no encontrado");

        User user = userOpt.get();
        user.setPassword(nuevaPassword);
        userService.guardarUsuario(user);

        return ResponseEntity.ok(Collections.singletonMap("message", "Contraseña actualizada correctamente"));
    }

    @Operation(summary = "Actualizar mi perfil")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<Map<String, String>> actualizarMiPerfil(@Valid @RequestBody UserRequestDTO dto,
                                                                   Authentication auth) {
        Optional<User> userOpt = userService.obtenerUsuarioPorEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(404).build();

        User user = userOpt.get();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        User actualizado = userService.actualizarUsuario(user.getId(), user);
        String nuevoToken = jwtUtils.generateJwtToken(actualizado.getEmail(), actualizado.getRoles());

        return ResponseEntity.ok(Map.of(
                "message", "Perfil actualizado correctamente",
                "token", nuevoToken
        ));
    }

    @Operation(summary = "Conviértete en creador")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/creator")
    public ResponseEntity<Map<String, String>> convertirseEnCreador(Authentication auth) {
        Optional<User> userOpt = userService.obtenerUsuarioPorEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(404).build();

        User user = userOpt.get();
        if (!user.getRoles().contains("ROLE_CREATOR")) {
            String nuevosRoles = user.getRoles() + ",ROLE_CREATOR";
            user.setRoles(nuevosRoles);
            userService.guardarUsuario(user);
        }

        String nuevoToken = jwtUtils.generateJwtToken(user.getEmail(), user.getRoles());
        return ResponseEntity.ok(Map.of("token", nuevoToken));
    }

    private UserDTO toDTO(User u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getEmail(), u.getRoles());
    }
}
