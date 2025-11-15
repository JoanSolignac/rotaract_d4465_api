package com.unapi.rotaract.rotaract_d4465_api.inscripcion.controller;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.service.InscripcionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionServiceImpl inscripcionService;

    @PostMapping("/{inscripcionId}/aceptar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    public ResponseEntity<String> aceptar(@PathVariable Long inscripcionId) {
        inscripcionService.aceptarInscripcion(inscripcionId);
        return ResponseEntity.ok("Inscripción aceptada.");
    }

    @PostMapping("/{inscripcionId}/rechazar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    public ResponseEntity<String> rechazar(@PathVariable Long inscripcionId) {
        inscripcionService.rechazarInscripcion(inscripcionId);
        return ResponseEntity.ok("Inscripción rechazada.");
    }
}
