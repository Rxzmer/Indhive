package com.indhive.controller;

import com.indhive.model.Proyecto;
import com.indhive.service.ProyectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping
    public List<Proyecto> listar() {
        return proyectoService.listarProyectos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> obtenerPorId(@PathVariable Long id) {
        return proyectoService.obtenerProyectoPorId(id)
                .map(proyecto -> ResponseEntity.ok().body(proyecto))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Proyecto crear(@RequestBody Proyecto proyecto) {
        return proyectoService.guardarProyecto(proyecto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> actualizar(@PathVariable Long id, @RequestBody Proyecto proyecto) {
        return proyectoService.obtenerProyectoPorId(id)
                .map(proyectoExistente -> {
                    proyectoExistente.setNombre(proyecto.getNombre());
                    proyectoExistente.setDescripcion(proyecto.getDescripcion());
                    proyectoExistente.setUsuarios(proyecto.getUsuarios());
                    Proyecto actualizado = proyectoService.guardarProyecto(proyectoExistente);
                    return ResponseEntity.ok().body(actualizado);
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (proyectoService.obtenerProyectoPorId(id).isPresent()) {
            proyectoService.eliminarProyecto(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
