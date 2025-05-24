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

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Cualquiera autenticado puede listar proyectos
    public List<Project> listar() {
        return projectService.listarProyectos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> obtenerPorId(@PathVariable Long id) {
        return projectService.obtenerProyectoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CREADOR', 'ADMIN')")  // Solo roles CREADOR o ADMIN pueden crear
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CREADOR', 'ADMIN')") // Solo roles CREADOR o ADMIN pueden actualizar
    public ResponseEntity<Project> actualizar(@PathVariable Long id,
                                              @RequestBody Project project,
                                              Authentication authentication) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project proyectoExistente = proyectoOpt.get();

        String username = authentication.getName();
        if (!proyectoExistente.getOwner().getUsername().equals(username)) {
            return ResponseEntity.status(403).build();
        }

        proyectoExistente.setTitle(project.getTitle());
        proyectoExistente.setDescription(project.getDescription());
        proyectoExistente.setCollaborators(project.getCollaborators());

        Project actualizado = projectService.guardarProyecto(proyectoExistente);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CREADOR', 'ADMIN')") // Solo roles CREADOR o ADMIN pueden eliminar
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project proyecto = proyectoOpt.get();

        String username = authentication.getName();
        if (!proyecto.getOwner().getUsername().equals(username)) {
            return ResponseEntity.status(403).build();
        }

        projectService.eliminarProyecto(id);
        return ResponseEntity.ok().build();
    }
}
