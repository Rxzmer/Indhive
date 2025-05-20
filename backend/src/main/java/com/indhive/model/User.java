package com.indhive.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String email;

    // rxzmer: relación Many-to-Many con Project
    // rxzmer: aquí se mapea la relación inversa de 'collaborators' en Project
    // rxzmer: un usuario puede colaborar en muchos proyectos
    @ManyToMany(mappedBy = "collaborators")
    private Set<Project> collaboratedProjects = new HashSet<>();

    // rxzmer: relación One-to-Many con Project
    // rxzmer: un usuario puede ser dueño (propietario) de muchos proyectos
    @OneToMany(mappedBy = "owner")
    @JsonManagedReference
    private Set<Project> ownedProjects = new HashSet<>();

    // Constructores
    public User() {}

    public User(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Project> getCollaboratedProjects() {
        return collaboratedProjects;
    }

    public void setCollaboratedProjects(Set<Project> collaboratedProjects) {
        this.collaboratedProjects = collaboratedProjects;
    }

    public Set<Project> getOwnedProjects() {
        return ownedProjects;
    }

    public void setOwnedProjects(Set<Project> ownedProjects) {
        this.ownedProjects = ownedProjects;
    }
}
