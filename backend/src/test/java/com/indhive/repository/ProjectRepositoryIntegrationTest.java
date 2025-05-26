package com.indhive.repository;

import com.indhive.model.Project;
import com.indhive.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ProjectRepositoryIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testGuardarYBuscarProyecto() {
        // Crear y guardar un usuario para asignar como owner
        User owner = new User();
        owner.setUsername("testuser");
        owner.setEmail("testuser@example.com");
        owner.setPassword("password");
        userRepository.save(owner);

        // Crear proyecto y asignar owner
        Project project = new Project();
        project.setTitle("Proyecto DB");
        project.setOwner(owner);  // asignar owner para evitar error NOT NULL

        // Guardar proyecto
        projectRepository.save(project);

        // Buscar proyecto por id
        Optional<Project> encontrado = projectRepository.findById(project.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Proyecto DB", encontrado.get().getTitle());
        assertEquals("testuser", encontrado.get().getOwner().getUsername());
    }
}
