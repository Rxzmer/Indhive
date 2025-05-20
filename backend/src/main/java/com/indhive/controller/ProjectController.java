
package com.indhive.controller;

import com.indhive.model.Project;
import com.indhive.model.User;
import com.indhive.service.ProjectService;
import com.indhive.service.UserService; // <-- rxzmer: para buscar el usuario simuladamente

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/proyectos")
public class ProjectController {

    @Autowired
    private ProjectService proyectoService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Project> listar() {
        // rxzmer: devuelve la lista completa de proyectos sin filtro ni paginación
        return proyectoService.listarProyectos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> obtenerPorId(@PathVariable Long id) {
        // rxzmer: busca proyecto por ID y devuelve 404 si no existe
        return proyectoService.obtenerProyectoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Project crear(@RequestBody Project proyecto, @RequestHeader("X-User-Id") Long userId) {
        // rxzmer: obtiene usuario propietario a partir del header y lo asigna como owner del proyecto
        Optional<User> userOpt = userService.obtenerUsuarioPorId(userId);
        userOpt.ifPresent(proyecto::setOwner);
        // rxzmer: guarda el nuevo proyecto con el propietario asignado
        return proyectoService.guardarProyecto(proyecto);
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/{id}")
    public ResponseEntity<Project> actualizar(@PathVariable Long id,
                                              @RequestBody Project proyecto,
                                              @RequestHeader("X-User-Id") Long userId) {
        // rxzmer: solo el propietario puede actualizar el proyecto, retorna 403 si no tiene permiso
        return proyectoService.obtenerProyectoPorId(id)
                .map(proyectoExistente -> {
                    if (!proyectoExistente.getOwner().getId().equals(userId)) {
                        return ResponseEntity.status(403).build(); // Forbidden
                    }
                    // rxzmer: actualiza los campos permitidos y colaboradores
                    proyectoExistente.setTitle(proyecto.getTitle());
                    proyectoExistente.setDescription(proyecto.getDescription());
                    proyectoExistente.setCollaborators(proyecto.getCollaborators());
                    Project actualizado = proyectoService.guardarProyecto(proyectoExistente);
                    return ResponseEntity.ok(actualizado);
                }).map(response -> (ResponseEntity<Project>) response)
                .orElse(ResponseEntity.notFound().build()); // rxzmer: retorna 404 si el proyecto no existe
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        // rxzmer: solo el propietario puede eliminar el proyecto, retorna 403 si no tiene permiso
        return proyectoService.obtenerProyectoPorId(id)
                .map(proyecto -> {
                    if (!proyecto.getOwner().getId().equals(userId)) {
                        return ResponseEntity.status(403).build(); // forbidden
                    }
                    // rxzmer: elimina el proyecto si el usuario tiene permisos
                    proyectoService.eliminarProyecto(id);
                    return ResponseEntity.<Void>ok().build();
                }).orElse(ResponseEntity.notFound().build()); // rxzmer: retorna 404 si el proyecto no existe
    }
}
