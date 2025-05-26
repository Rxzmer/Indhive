package com.indhive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indhive.model.Project;
import com.indhive.model.User;
import com.indhive.service.ProjectService;
import com.indhive.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Project project;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("admin");

        project = new Project();
        project.setId(1L);
        project.setTitle("Proyecto Test");
        project.setDescription("Descripción Test");
        project.setOwner(user);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCrearProyecto() throws Exception {
        when(userService.obtenerUsuarioPorUsername("admin")).thenReturn(Optional.of(user));
        when(projectService.guardarProyecto(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(1L);
            p.setOwner(user);  // Aseguramos que owner se setea
            return p;
        });

        Project proyectoCrear = new Project();
        proyectoCrear.setTitle("Nuevo Proyecto");
        proyectoCrear.setDescription("Descripción Nuevo Proyecto");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proyectoCrear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Nuevo Proyecto"))
                .andExpect(jsonPath("$.description").value("Descripción Nuevo Proyecto"))
                .andExpect(jsonPath("$.owner.username").value("admin"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testObtenerProyectoPorId() throws Exception {
        when(projectService.obtenerProyectoPorId(1L)).thenReturn(Optional.of(project));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Proyecto Test"))
                .andExpect(jsonPath("$.description").value("Descripción Test"))
                .andExpect(jsonPath("$.owner.username").value("admin"));
    }
}
