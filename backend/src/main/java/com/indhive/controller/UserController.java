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
@RequestMapping("/api/usuarios")
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
        return userOpt.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo usuario
    @PostMapping
    public User crear(@RequestBody User usuario) {
        return userService.guardarUsuario(usuario);
    }

    // Actualizar usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<User> actualizar(@PathVariable Long id, @RequestBody User usuario) {
        return userService.obtenerUsuarioPorId(id)
                .map(u -> {
                    u.setUsername(usuario.getUsername());
                    u.setEmail(usuario.getEmail());
                    u.setRole(usuario.getRole());
                    return ResponseEntity.ok(userService.guardarUsuario(u));
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
    @GetMapping("/{id}/proyectos")
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
