package com.indhive.dto;

import java.util.Set;

public class ProjectDTO {
    private Long id;
    private String title;
    private String description;
    private String ownerUsername;
    private Set<String> collaboratorUsernames;

    public ProjectDTO() {}

    public ProjectDTO(Long id, String title, String description, String ownerUsername, Set<String> collaboratorUsernames) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.collaboratorUsernames = collaboratorUsernames;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Set<String> getCollaboratorUsernames() { return collaboratorUsernames; }
    public void setCollaboratorUsernames(Set<String> collaboratorUsernames) { this.collaboratorUsernames = collaboratorUsernames; }
}
