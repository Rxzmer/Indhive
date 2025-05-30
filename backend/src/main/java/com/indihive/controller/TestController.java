package com.indihive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Test", description = "Controlador de prueba para verificar Swagger")
public class TestController {

    @Operation(summary = "Saludo de prueba")
    @GetMapping("/hello")
    public String hello() {
        return "¡Hola desde Swagger!";
    }
}
