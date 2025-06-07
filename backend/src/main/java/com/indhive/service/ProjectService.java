package com.indhive.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.indhive.model.Project;
import com.indhive.repository.ProjectCollaboratorRepository;
import com.indhive.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
@Transactional  // Mueve esta anotación aquí
public class ProjectService {

    @Autowired
    private ProjectRepository proyectoRepository;

    @Autowired
    private ProjectCollaboratorRepository collaboratorRepository;

    @Autowired
    private EntityManager entityManager;

    // Obtener todos los proyectos
    public List<Project> listarProyectos() {
        return proyectoRepository.findAll();
    }

    // Buscar por ID
    public Optional<Project> obtenerProyectoPorId(Long id) {
        return proyectoRepository.findById(id);
    }

    // Guardar o actualizar
    public Project guardarProyecto(Project proyecto) {
        return proyectoRepository.save(proyecto);
    }

    /**
     * Elimina un proyecto junto con sus colaboradores asociados.
     * Valida que el proyecto exista antes de eliminar.
     * 
     * @param id ID del proyecto a eliminar
     */
    public void eliminarProyecto(Long id) {
        // 1. Eliminar primero los colaboradores
        collaboratorRepository.deleteByProjectId(id);
        
        // 2. Limpiar el caché de Hibernate
        entityManager.flush();
        entityManager.clear();
        
        // 3. Eliminar el proyecto
        proyectoRepository.deleteById(id);
        
        // 4. Forzar sincronización con la base de datos
        entityManager.flush();
    }
}