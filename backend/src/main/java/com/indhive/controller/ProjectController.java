package com.indhive.controller;

import com.indhive.model.Project;
import com.indhive.model.User;
import com.indhive.service.ProjectService;
import com.indhive.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    // Listar todos los proyectos (cualquiera autenticado)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Project> listar() {
        return projectService.listarProyectos();
    }

    // Obtener proyecto por id (cualquiera autenticado)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> obtenerPorId(@PathVariable Long id) {
        return projectService.obtenerProyectoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear proyecto (solo ADMIN o CREATOR)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<?> crear(@RequestBody Project project, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.obtenerUsuarioPorUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        User user = userOpt.get();
        project.setOwner(user);
        Project creado = projectService.guardarProyecto(project);
        return ResponseEntity.ok(creado);
    }

    // Actualizar proyecto (solo ADMIN o CREATOR y dueño)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<Project> actualizar(@PathVariable Long id,
                                              @RequestBody Project project,
                                              Authentication authentication) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project proyectoExistente = proyectoOpt.get();

        String username = authentication.getName();

        // Validar que sea dueño para actualizar
        if (!proyectoExistente.getOwner().getUsername().trim().equalsIgnoreCase(username.trim())) {
            return ResponseEntity.status(403).build();
        }

        proyectoExistente.setTitle(project.getTitle());
        proyectoExistente.setDescription(project.getDescription());
        proyectoExistente.setCollaborators(project.getCollaborators());

        Project actualizado = projectService.guardarProyecto(proyectoExistente);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar proyecto (solo ADMIN o CREATOR y dueño)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project proyecto = proyectoOpt.get();

        String username = authentication.getName();
        User owner = proyecto.getOwner();

        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = owner != null && owner.getUsername() != null &&
                          owner.getUsername().trim().equalsIgnoreCase(username.trim());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        projectService.eliminarProyecto(id);
        return ResponseEntity.ok().build();
    }
}
