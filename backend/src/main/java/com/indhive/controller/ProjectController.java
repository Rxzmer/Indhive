package com.indhive.controller;

import com.indhive.model.Project;
import com.indhive.model.User;
import com.indhive.service.ProjectService;
import com.indhive.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")  // en inglés para consistencia
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Project> listar() {
        return projectService.listarProyectos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> obtenerPorId(@PathVariable Long id) {
        return projectService.obtenerProyectoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Project project,
                                  @RequestHeader("X-User-Id") Long userId) {
        Optional<User> userOpt = userService.obtenerUsuarioPorId(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        User user = userOpt.get();

        // Validar si el rol contiene "CREADOR"
        boolean tieneRolCreador = user.getRoles() != null &&
                                  user.getRoles().toUpperCase().contains("CREADOR");

        if (!tieneRolCreador) {
            return ResponseEntity.status(403).body("Solo usuarios con rol CREADOR pueden crear proyectos");
        }

        project.setOwner(user);
        Project creado = projectService.guardarProyecto(project);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> actualizar(@PathVariable Long id,
                                              @RequestBody Project project,
                                              @RequestHeader("X-User-Id") Long userId) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project proyectoExistente = proyectoOpt.get();

        if (!proyectoExistente.getOwner().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        proyectoExistente.setTitle(project.getTitle());
        proyectoExistente.setDescription(project.getDescription());
        proyectoExistente.setCollaborators(project.getCollaborators());

        Project actualizado = projectService.guardarProyecto(proyectoExistente);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @RequestHeader("X-User-Id") Long userId) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project proyecto = proyectoOpt.get();

        if (!proyecto.getOwner().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        projectService.eliminarProyecto(id);
        return ResponseEntity.ok().build();
    }
}
