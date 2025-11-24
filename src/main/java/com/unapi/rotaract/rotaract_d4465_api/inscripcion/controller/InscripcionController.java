package com.unapi.rotaract.rotaract_d4465_api.inscripcion.controller;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Operaciones para gestionar y consultar inscripciones")
public class InscripcionController {

    private final IInscripcionService inscripcionService;

    @GetMapping("/mis")
    @PreAuthorize("hasAnyRole('INTERESADO', 'SOCIO', 'PRESIDENTE')")
    @Operation(summary = "Listar mis inscripciones", description = "Devuelve una página con las inscripciones del usuario autenticado (convocatorias y proyectos).")
    public ResponseEntity<Page<InscripcionResponseDto>> listarMisInscripciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(inscripcionService.listarMisInscripciones(page, size));
    }
}

