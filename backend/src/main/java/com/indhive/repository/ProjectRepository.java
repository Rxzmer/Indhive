package com.indhive.repository;

import com.indhive.model.Project;
import com.indhive.model.User;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @EntityGraph(attributePaths = { "owner", "collaborators" })
    @org.springframework.lang.NonNull
    List<Project> findAll();

    // 👇 Este es el nuevo método necesario para eliminar proyectos de un usuario
    List<Project> findByOwner(User owner);
}
