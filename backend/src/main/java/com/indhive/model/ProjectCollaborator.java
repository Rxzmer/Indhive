package com.indhive.model;

import jakarta.persistence.*;

@Entity
@Table(name = "project_collaborators")
public class ProjectCollaborator {

    @EmbeddedId
    private ProjectCollaboratorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_project_collaborators_project"))
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_project_collaborators_user"))
    private User user;

    public ProjectCollaborator() {}

    public ProjectCollaborator(Project project, User user) {
        this.project = project;
        this.user = user;
        this.id = new ProjectCollaboratorId(project.getId(), user.getId());
    }

    public ProjectCollaboratorId getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public User getUser() {
        return user;
    }
    
}
