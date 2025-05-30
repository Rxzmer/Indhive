package com.indhive.service;

import com.indhive.model.Project;
import com.indhive.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository proyectoRepository;

    @InjectMocks
    private ProjectService proyectoService;

    @Test
    public void testListarProyectos() {
        List<Project> listaMock = List.of(new Project(), new Project());
        when(proyectoRepository.findAll()).thenReturn(listaMock);

        List<Project> resultado = proyectoService.listarProyectos();

        assertEquals(2, resultado.size());
        verify(proyectoRepository, times(1)).findAll();
    }

    @Test
    public void testObtenerProyectoPorId() {
        Project proyecto = new Project();
        proyecto.setId(1L);
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));

        Optional<Project> resultado = proyectoService.obtenerProyectoPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    public void testGuardarProyecto() {
        Project proyecto = new Project();
        proyecto.setTitle("Proyecto Test");

        when(proyectoRepository.save(any(Project.class))).thenReturn(proyecto);

        Project resultado = proyectoService.guardarProyecto(proyecto);

        assertNotNull(resultado);
        assertEquals("Proyecto Test", resultado.getTitle());
        verify(proyectoRepository, times(1)).save(proyecto);
    }

    @Test
    public void testEliminarProyecto() {
        Long id = 1L;
        doNothing().when(proyectoRepository).deleteById(id);

        proyectoService.eliminarProyecto(id);

        verify(proyectoRepository, times(1)).deleteById(id);
    }
}
