package com.unapi.rotaract.rotaract_d4465_api.asistencia.controller;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.HistorialAsistenciaDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.interfaces.IAsistenciaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {

    private final IAsistenciaService asistenciaService;

    // ============================================================
    // ÚNICO ENDPOINT: HISTORIAL DE ASISTENCIAS DEL USUARIO
    // ============================================================

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('SOCIO','PRESIDENTE')")
    public ResponseEntity<List<HistorialAsistenciaDto>> obtenerHistorialAsistencias() {
        return ResponseEntity.ok(asistenciaService.obtenerHistorialAsistencias());
    }

}
