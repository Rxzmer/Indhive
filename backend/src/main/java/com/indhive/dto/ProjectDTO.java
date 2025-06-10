package com.indhive.dto;

import java.util.List; 

public class ProjectDTO {

    private Long id;
    private String title;
    private String description;
    private Long ownerId;
    private String ownerUsername;
    private List<SimpleUserDTO> collaborators; 

    public ProjectDTO() {}

    public ProjectDTO(Long id, String title, String description,
                      Long ownerId, String ownerUsername,
                      List<SimpleUserDTO> collaborators) { 
        this.title = title;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.collaborators = collaborators;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public List<SimpleUserDTO> getCollaborators() { // CAMBIADO
        return collaborators;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setCollaborators(List<SimpleUserDTO> collaborators) { // CAMBIADO
        this.collaborators = collaborators;
    }
}
