package com.indhive.controller;

import com.indhive.model.User;
import com.indhive.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Listar todos los usuarios",
               description = "Devuelve la lista completa de usuarios. Solo accesible para usuarios con rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = User.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> listar() {
        return userService.listarUsuarios();
    }

    @Operation(summary = "Obtener usuario por ID",
               description = "Devuelve la información de un usuario específico, excluyendo la contraseña. Accesible para cualquier usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerPorId(@PathVariable Long id) {
        Optional<User> userOpt = userService.obtenerUsuarioPorId(id);
        return userOpt.map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un nuevo usuario",
               description = "Permite crear un nuevo usuario. Solo accesible para usuarios con rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario creado correctamente",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = User.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> crear(@RequestBody User usuario) {
        if (usuario.getRoles() == null || usuario.getRoles().isBlank()) {
            usuario.setRoles("ROLE_USER");
        }
        User savedUser = userService.guardarUsuario(usuario);
        savedUser.setPassword(null);
        return ResponseEntity.ok(savedUser);
    }

    @Operation(summary = "Actualizar un usuario existente",
               description = "Actualiza la información de un usuario. Solo accesible para usuarios con rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<User> actualizar(@PathVariable Long id, @RequestBody User usuario) {
        return userService.obtenerUsuarioPorId(id)
                .map(u -> {
                    u.setUsername(usuario.getUsername());
                    u.setEmail(usuario.getEmail());
                    u.setRoles(usuario.getRoles());
                    if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
                        u.setPassword(passwordEncoder.encode(usuario.getPassword()));
                    }
                    User updated = userService.guardarUsuario(u);
                    updated.setPassword(null);
                    return ResponseEntity.ok(updated);
                }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un usuario",
               description = "Elimina un usuario por su ID. Solo accesible para usuarios con rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (userService.obtenerUsuarioPorId(id).isPresent()) {
            userService.eliminarUsuario(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Listar proyectos de un usuario",
               description = "Devuelve los proyectos que un usuario posee y en los que colabora. Accesible para cualquier usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Proyectos obtenidos correctamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @GetMapping("/{id}/projects")
    public ResponseEntity<Map<String, Object>> listarProyectosDeUsuario(@PathVariable Long id) {
        Optional<User> userOpt = userService.obtenerUsuarioPorId(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("ownedProjects", user.getOwnedProjects());
        response.put("collaboratedProjects", user.getCollaboratedProjects());

        return ResponseEntity.ok(response);
    }
}
