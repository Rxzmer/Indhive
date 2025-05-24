package com.indhive.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public ResponseEntity<String> home() {
        // rxzmer: punto de entrada básico para verificar que el backend está corriendo
        // rxzmer: retorna un mensaje de bienvenida con status HTTP 200 OK
        return ResponseEntity.ok("Bienvenido a Indhive Backend");
    }
}
