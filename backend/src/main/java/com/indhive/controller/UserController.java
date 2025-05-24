package com.indhive.controller;

import com.indhive.model.User;
import com.indhive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")  // Cambié a inglés para consistencia
public class UserController {

    @Autowired
    private UserService userService;

    // Obtener todos los usuarios
    @GetMapping
    public List<User> listar() {
        return userService.listarUsuarios();
    }

    // Obtener usuario por id
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerPorId(@PathVariable Long id) {
        Optional<User> userOpt = userService.obtenerUsuarioPorId(id);
        return userOpt.map(user -> {
                    user.setPassword(null);  // No devolver la contraseña
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo usuario
    @PostMapping
public ResponseEntity<User> crear(@RequestBody User usuario) {
    if (usuario.getRoles() == null || usuario.getRoles().isBlank()) {
        usuario.setRoles("ROLE_USER");  // Valor por defecto si está vacío
    }
    User savedUser = userService.guardarUsuario(usuario);
    savedUser.setPassword(null);
    return ResponseEntity.ok(savedUser);
}

@PutMapping("/{id}")
public ResponseEntity<User> actualizar(@PathVariable Long id, @RequestBody User usuario) {
    return userService.obtenerUsuarioPorId(id)
            .map(u -> {
                u.setUsername(usuario.getUsername());
                u.setEmail(usuario.getEmail());
                u.setRoles(usuario.getRoles());
                User updated = userService.guardarUsuario(u);
                updated.setPassword(null);
                return ResponseEntity.ok(updated);
            }).orElse(ResponseEntity.notFound().build());
}

    // Eliminar usuario por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (userService.obtenerUsuarioPorId(id).isPresent()) {
            userService.eliminarUsuario(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Listar proyectos de un usuario
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
