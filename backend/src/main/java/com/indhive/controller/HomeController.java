package com.indhive.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Tag(name = "Inicio", description = "Controlador para la ruta raíz y mensajes básicos del backend")
public class HomeController {

    @Operation(summary = "Mensaje de bienvenida",
               description = "Devuelve un mensaje básico para verificar que el backend está corriendo")
    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Bienvenido a Indhive Backend");
    }
}
