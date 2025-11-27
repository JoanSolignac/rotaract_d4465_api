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
    // 1. HISTORIAL DE ASISTENCIAS DEL USUARIO (SOCIO / PRESIDENTE)
    // ============================================================

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('SOCIO','PRESIDENTE')")
    public ResponseEntity<List<HistorialAsistenciaDto>> obtenerHistorialAsistencias() {
        return ResponseEntity.ok(asistenciaService.obtenerHistorialAsistencias());
    }

    // ============================================================
    // 2. EXPORTAR ASISTENCIAS DE UN PROYECTO A EXCEL (PRESIDENTE)
    // ============================================================

    /**
     * Exporta las asistencias de un proyecto a un archivo Excel (.xlsx)
     * y lo devuelve como archivo descargable.
     *
     * @param proyectoId identificador del proyecto
     * @return archivo Excel en bytes
     */
    @GetMapping("/{proyectoId}/excel")
    @PreAuthorize("hasRole('PRESIDENTE')")
    public ResponseEntity<byte[]> exportarExcelAsistencias(
            @PathVariable Long proyectoId
    ) {
        byte[] excel = asistenciaService.exportarExcelAsistencias(proyectoId);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=asistencias_proyecto_" + proyectoId + ".xlsx")
                .header("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }

}
