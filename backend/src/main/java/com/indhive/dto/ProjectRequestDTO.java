package com.indhive.dto;

import java.util.Set;

import jakarta.validation.constraints.Size;

public class ProjectRequestDTO {
    private String title;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String description;
    private Set<Long> collaboratorIds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getCollaboratorIds() {
        return collaboratorIds;
    }

    public void setCollaboratorIds(Set<Long> collaboratorIds) {
        this.collaboratorIds = collaboratorIds;
    }
}
