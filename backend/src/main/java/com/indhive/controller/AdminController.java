package com.indhive.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/only")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String onlyForSuperadmins() {
        return "🔐 Acceso concedido SOLO a SUPERADMIN";
    }
}
