package com.indhive.repository;

import com.indhive.model.Project;
import com.indhive.model.User;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;  // Asegúrate de que esta importación esté presente
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    @EntityGraph(attributePaths = { "owner", "collaborators" })
    @org.springframework.lang.NonNull
    List<Project> findAll();

    List<Project> findByOwner(User owner);

    // Eliminar la relación de un colaborador con un proyecto
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectCollaborator pc WHERE pc.project.id = :projectId AND pc.user.id = :userId")
    void deleteCollaborator(@Param("projectId") Long projectId, @Param("userId") Long userId);

    // Eliminar un proyecto por ID (sin eliminar los colaboradores, solo el proyecto)
    @Modifying
    @Transactional
    @Query("DELETE FROM Project p WHERE p.id = :id")
    void deleteById(Long id);
}
