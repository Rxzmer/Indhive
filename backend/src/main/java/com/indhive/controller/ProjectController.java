package com.indhive.controller;

import com.indhive.dto.ProjectDTO;
import com.indhive.dto.ProjectRequestDTO;
import com.indhive.model.Project;
import com.indhive.model.User;
import com.indhive.service.ProjectService;
import com.indhive.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Proyectos", description = "Gestión de proyectos con DTOs")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDTO> listar() {
        return projectService.listarProyectos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectDTO> obtenerPorId(@PathVariable Long id) {
        return projectService.obtenerProyectoPorId(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<?> crear(@RequestBody ProjectRequestDTO dto, Authentication auth) {
        String email = auth.getName();
        Optional<User> ownerOpt = userService.obtenerUsuarioPorEmail(email);
        if (ownerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario autenticado no encontrado");
        }

        Set<User> colaboradores = new HashSet<>();
        if (dto.getCollaboratorIds() != null) {
            colaboradores = dto.getCollaboratorIds().stream()
                    .map(userService::obtenerUsuarioPorId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
        }

        Project nuevo = new Project(dto.getTitle(), dto.getDescription(), ownerOpt.get());
        nuevo.setCollaborators(colaboradores);

        Project guardado = projectService.guardarProyecto(nuevo);
        return ResponseEntity.ok(toDTO(guardado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ProjectRequestDTO dto, Authentication auth) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Project proyecto = proyectoOpt.get();
        String email = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = proyecto.getOwner().getEmail().equalsIgnoreCase(email);
        if (!isAdmin && !isOwner) return ResponseEntity.status(403).build();

        proyecto.setTitle(dto.getTitle());
        proyecto.setDescription(dto.getDescription());

        if (dto.getCollaboratorIds() != null) {
            Set<User> colaboradores = dto.getCollaboratorIds().stream()
                    .map(userService::obtenerUsuarioPorId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            proyecto.setCollaborators(colaboradores);
        }

        Project actualizado = projectService.guardarProyecto(proyecto);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Project proyecto = proyectoOpt.get();
        String email = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = proyecto.getOwner().getEmail().equalsIgnoreCase(email);
        if (!isAdmin && !isOwner) return ResponseEntity.status(403).build();

        projectService.eliminarProyecto(id);
        return ResponseEntity.ok().build();
    }

    private ProjectDTO toDTO(Project p) {
        Set<String> collaboratorNames = p.getCollaborators().stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

        return new ProjectDTO(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getOwner().getUsername(),
                collaboratorNames
        );
    }
}
