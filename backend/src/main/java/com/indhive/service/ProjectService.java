package com.indhive.service;

import com.indhive.model.Project;
import com.indhive.repository.ProjectRepository;
import com.indhive.repository.ProjectCollaboratorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository proyectoRepository;

    @Autowired
    private ProjectCollaboratorRepository collaboratorRepository;

    // rxzmer: obtiene la lista completa de proyectos
    public List<Project> listarProyectos() {
        return proyectoRepository.findAll();
    }

    // rxzmer: busca un proyecto por su ID
    public Optional<Project> obtenerProyectoPorId(Long id) {
        return proyectoRepository.findById(id);
    }

    // rxzmer: guarda o actualiza un proyecto en la base de datos
    public Project guardarProyecto(Project proyecto) {
        return proyectoRepository.save(proyecto);
    }

    // rxzmer: elimina un proyecto y sus relaciones
    @Transactional
public void eliminarProyecto(Long id) {
    collaboratorRepository.deleteByProjectId(id);

    proyectoRepository.findById(id).ifPresent(proyecto -> {
        proyectoRepository.delete(proyecto);
        proyectoRepository.flush();
        System.out.println("✅ Proyecto eliminado: " + proyecto.getTitle());
    });
}


}
