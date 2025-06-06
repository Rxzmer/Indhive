package com.indhive.repository;

import com.indhive.model.ProjectCollaborator;
import com.indhive.model.ProjectCollaboratorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCollaboratorRepository extends JpaRepository<ProjectCollaborator, ProjectCollaboratorId> {

    List<ProjectCollaborator> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM ProjectCollaborator pc WHERE pc.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ProjectCollaborator pc WHERE pc.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
