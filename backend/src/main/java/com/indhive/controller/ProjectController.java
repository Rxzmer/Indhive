package com.indhive.controller;

import com.indhive.model.Project;
import com.indhive.model.User;
import com.indhive.service.ProjectService;
import com.indhive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Proyectos", description = "Gestión de proyectos: creación, edición, listado y eliminación")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Listar todos los proyectos",
               description = "Obtiene la lista completa de proyectos disponibles para usuarios autenticados")
    @ApiResponse(responseCode = "200", description = "Lista de proyectos", 
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = Project.class)))
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Project> listar() {
        return projectService.listarProyectos();
    }

    @Operation(summary = "Obtener proyecto por ID",
               description = "Obtiene la información detallada de un proyecto específico por su identificador")
    @ApiResponse(responseCode = "200", description = "Proyecto encontrado",
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = Project.class)))
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> obtenerPorId(@PathVariable Long id) {
        return projectService.obtenerProyectoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear nuevo proyecto",
               description = "Crea un nuevo proyecto asignado al usuario autenticado. Solo roles ADMIN o CREATOR pueden crear proyectos.")
    @ApiResponse(responseCode = "200", description = "Proyecto creado exitosamente",
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = Project.class)))
    @ApiResponse(responseCode = "400", description = "Error en los datos enviados")
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

    @Operation(summary = "Actualizar proyecto",
               description = "Actualiza un proyecto existente. Solo roles ADMIN o CREATOR que sean dueños pueden actualizar.")
    @ApiResponse(responseCode = "200", description = "Proyecto actualizado exitosamente",
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = Project.class)))
    @ApiResponse(responseCode = "403", description = "No autorizado para actualizar este proyecto")
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
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

        if (!proyectoExistente.getOwner().getUsername().trim().equalsIgnoreCase(username.trim())) {
            return ResponseEntity.status(403).build();
        }

        proyectoExistente.setTitle(project.getTitle());
        proyectoExistente.setDescription(project.getDescription());
        proyectoExistente.setCollaborators(project.getCollaborators());

        Project actualizado = projectService.guardarProyecto(proyectoExistente);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Eliminar proyecto",
               description = "Elimina un proyecto existente. Solo roles ADMIN o CREATOR que sean dueños pueden eliminar.")
    @ApiResponse(responseCode = "200", description = "Proyecto eliminado exitosamente")
    @ApiResponse(responseCode = "403", description = "No autorizado para eliminar este proyecto")
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
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
