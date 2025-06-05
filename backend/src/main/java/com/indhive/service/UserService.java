package com.indhive.service;

import com.indhive.model.User;
import com.indhive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    public Optional<User> obtenerUsuarioPorId(Long id) {
        return userRepository.findById(id);
    }

    // Guardar usuario y cifrar contraseña
    public User guardarUsuario(User usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return userRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> obtenerUsuarioPorUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> obtenerUsuarioPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User actualizarUsuario(Long id, User usuarioActualizado) {
    User usuarioExistente = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    usuarioExistente.setUsername(usuarioActualizado.getUsername());
    usuarioExistente.setEmail(usuarioActualizado.getEmail());

    if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
        usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
    }
    // Si no hay contraseña proporcionada, no modificar la existente

    return userRepository.save(usuarioExistente);
}

}
