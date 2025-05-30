package com.indhive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "proyectos")
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class,
  property = "id"
)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    // Relación Many-to-One con User, representa al dueño del proyecto (propietario)
    @ManyToOne
    @JoinColumn(
        name = "owner_id",
        nullable = false,
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_project_owner")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;

    // Relación Many-to-Many con usuarios colaboradores del proyecto
    @ManyToMany
    @JoinTable(
        name = "project_collaborators",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        foreignKey = @ForeignKey(name = "fk_project_collaborators_project"),
        inverseForeignKey = @ForeignKey(name = "fk_project_collaborators_user")
    )
    private Set<User> collaborators = new HashSet<>();

    // Constructores
    public Project() {}

    public Project(String title, String description, User owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
    }

    // Getters y Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public User getOwner() { return owner; }

    public void setOwner(User owner) { this.owner = owner; }

    public Set<User> getCollaborators() { return collaborators; }

    public void setCollaborators(Set<User> collaborators) { this.collaborators = collaborators; }
}
