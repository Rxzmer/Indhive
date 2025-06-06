package com.indhive.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "usuarios")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String roles = "ROLE_USER";

    private String password;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Project> ownedProjects = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectCollaborator> collaborations = new HashSet<>();

    public User() {}

    public User(String username, String email, String roles, String password) {
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.password = password;
    }

    // Getters y Setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getRoles() { return roles; }

    public void setRoles(String roles) {
        if (roles != null) {
            this.roles = Arrays.stream(roles.split(","))
                .map(role -> role.trim().startsWith("ROLE_") ? role.trim() : "ROLE_" + role.trim())
                .collect(Collectors.joining(","));
        } else {
            this.roles = null;
        }
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public Set<Project> getOwnedProjects() { return ownedProjects; }

    public void setOwnedProjects(Set<Project> ownedProjects) { this.ownedProjects = ownedProjects; }

    public Set<ProjectCollaborator> getCollaborations() { return collaborations; }

    public void setCollaborations(Set<ProjectCollaborator> collaborations) { this.collaborations = collaborations; }
}
