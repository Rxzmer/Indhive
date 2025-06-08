package com.indhive.controller;

import com.indhive.dto.ProjectDTO;
import com.indhive.dto.ProjectRequestDTO;
import com.indhive.dto.SimpleUserDTO;
import com.indhive.model.Project;
import com.indhive.model.ProjectCollaborator;
import com.indhive.model.User;
import com.indhive.service.ProjectService;
import com.indhive.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

        Project nuevo = new Project(dto.getTitle(), dto.getDescription(), ownerOpt.get());

        if (dto.getCollaboratorIds() != null) {
            for (Long userId : dto.getCollaboratorIds()) {
                userService.obtenerUsuarioPorId(userId).ifPresent(user -> {
                    ProjectCollaborator pc = new ProjectCollaborator(nuevo, user);
                    nuevo.getCollaborators().add(pc);
                });
            }
        }

        Project guardado = projectService.guardarProyecto(nuevo);
        return ResponseEntity.ok(toDTO(guardado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO dto,
            Authentication auth) {

        // Obtener proyecto
        Project proyecto = projectService.obtenerProyectoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Verificar permisos: si es ADMIN o propietario
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = proyecto.getOwner().getEmail().equalsIgnoreCase(email);

        // Solo los ADMIN o el propietario pueden editar
        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para editar este proyecto");
        }

        // Actualizar proyecto
        proyecto.setTitle(dto.getTitle());
        proyecto.setDescription(dto.getDescription());

        // Manejo de colaboradores
        if (dto.getCollaboratorIds() != null) {
            Set<Long> nuevosIds = new HashSet<>(dto.getCollaboratorIds());

            // Eliminar colaboradores que ya no están
            proyecto.getCollaborators().removeIf(pc -> !nuevosIds.contains(pc.getUser().getId()) &&
                    !pc.getUser().getId().equals(proyecto.getOwner().getId()));

            // Añadir nuevos colaboradores
            Set<Long> idsActuales = proyecto.getCollaborators().stream()
                    .map(pc -> pc.getUser().getId())
                    .collect(Collectors.toSet());

            dto.getCollaboratorIds().stream()
                    .filter(userId -> !idsActuales.contains(userId))
                    .forEach(userId -> userService.obtenerUsuarioPorId(userId)
                            .ifPresent(user -> {
                                if (!user.getId().equals(proyecto.getOwner().getId())) {
                                    ProjectCollaborator pc = new ProjectCollaborator(proyecto, user);
                                    proyecto.getCollaborators().add(pc);
                                }

                            }));
        }

        // Guardar y retornar
        Project actualizado = projectService.guardarProyecto(proyecto);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        Optional<Project> proyectoOpt = projectService.obtenerProyectoPorId(id);
        if (proyectoOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Project proyecto = proyectoOpt.get();
        String email = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = proyecto.getOwner().getEmail().equalsIgnoreCase(email);
        if (!isAdmin && !isOwner)
            return ResponseEntity.status(403).build();

        projectService.eliminarProyecto(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina un colaborador de un proyecto.
     * 
     * @param projectId ID del proyecto
     * @param userId    ID del colaborador
     * @return Respuesta HTTP con el resultado de la operación
     */
    @DeleteMapping("/{projectId}/collaborators/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isOwnerOrCollaborator(#projectId, authentication.name)")
    public ResponseEntity<?> eliminarColaborador(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        Optional<Project> projectOpt = projectService.obtenerProyectoPorId(projectId);
        Optional<User> userOpt = userService.obtenerUsuarioPorId(userId);

        if (projectOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Llamar al servicio para eliminar el colaborador
            projectService.eliminarColaborador(projectId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Colaborador eliminado correctamente",
                    "projectId", projectId,
                    "userId", userId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error al eliminar colaborador");
        }
    }

    private ProjectDTO toDTO(Project p) {
        Set<SimpleUserDTO> collaborators = p.getCollaborators().stream()
                .map(pc -> new SimpleUserDTO(
                        pc.getUser().getId(),
                        pc.getUser().getUsername()
                ))
                .collect(Collectors.toSet());

        return new ProjectDTO(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getOwner().getId(),
                p.getOwner().getUsername(),
                collaborators
        );
    }
}

