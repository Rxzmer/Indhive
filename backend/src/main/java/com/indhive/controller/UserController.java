package com.indhive.controller;

import com.indhive.model.User;
import com.indhive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    @Autowired
    private UserService userService;

    // rxzmer: obtener todos los usuarios
    @GetMapping
    public List<User> listar() {
        return userService.listarUsuarios();
    }

    // rxzmer: obtener un usuario por su ID
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerPorId(@PathVariable Long id) {
        Optional<User> userOpt = userService.obtenerUsuarioPorId(id);
        return userOpt.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // rxzmer: crear un nuevo usuario
    @PostMapping
    public User crear(@RequestBody User usuario) {
        return userService.guardarUsuario(usuario);
    }

    // rxzmer: actualizar un usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<User> actualizar(@PathVariable Long id, @RequestBody User usuario) {
        return userService.obtenerUsuarioPorId(id)
                .map(u -> {
                    u.setNombre(usuario.getNombre());
                    u.setEmail(usuario.getEmail());
                    return ResponseEntity.ok(userService.guardarUsuario(u));
                }).orElse(ResponseEntity.notFound().build());
    }

    // rxzmer: eliminar un usuario por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (userService.obtenerUsuarioPorId(id).isPresent()) {
            userService.eliminarUsuario(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
