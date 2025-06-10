package com.indhive.service;

import com.indhive.dto.UserDTO;
import com.indhive.dto.UserRequestDTO;
import com.indhive.model.Project;
import com.indhive.model.User;
import com.indhive.model.ProjectCollaborator;
import com.indhive.repository.ProjectRepository;
import com.indhive.repository.UserRepository;
import com.indhive.repository.ProjectCollaboratorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectCollaboratorRepository collaboratorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    public Optional<User> obtenerUsuarioPorId(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> obtenerUsuarioPorUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> obtenerUsuarioPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User guardarUsuario(User usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return userRepository.save(usuario);
    }

    // Actualiza un usuario usando UserRequestDTO
    public User actualizarUsuario(Long id, UserRequestDTO dto, boolean isAdmin) {
        User usuarioExistente = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setEmail(dto.getEmail());

        // Solo actualiza la contraseña si se ha proporcionado una nueva
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuarioExistente.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Solo los admins pueden cambiar el rol
        if (isAdmin && dto.getRoles() != null) {
            usuarioExistente.setRoles(dto.getRoles());
        }

        return userRepository.save(usuarioExistente);
    }

    // Método anterior de actualización, ahora no es necesario
    public User actualizarUsuarioDesdeDTO(Long id, UserDTO dto) {
        User usuarioExistente = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setEmail(dto.getEmail());

        if (dto.getRoles() != null && !dto.getRoles().isBlank()) {
            usuarioExistente.setRoles(dto.getRoles());
        }

        return userRepository.save(usuarioExistente);
    }

    public List<Project> buscarColaboraciones(Long userId) {
        return collaboratorRepository.findByUserId(userId).stream()
                .map(ProjectCollaborator::getProject)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();

        // 1. Eliminar colaboraciones (project_collaborators)
        collaboratorRepository.deleteByUserId(user.getId());

        // 2. Eliminar proyectos que posee el usuario directamente
        List<Project> owned = projectRepository.findByOwner(user);
        projectRepository.deleteAll(owned);

        // 3. Eliminar usuario
        userRepository.delete(user);
    }
}
