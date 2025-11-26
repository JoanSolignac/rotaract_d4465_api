package com.unapi.rotaract.rotaract_d4465_api.miembro.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.MiembroResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.RepresentanteResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.TotalUsuariosResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.interfaces.IMiembroService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/miembros")
@RequiredArgsConstructor
public class MiembroController {

    private final IMiembroService miembroService;

    /**
     * LISTADO PAGINADO (solo Representante Distrital)
     */
    @PreAuthorize("hasAuthority('REPRESENTANTE DISTRITAL')")
    @GetMapping("/paginado")
    public ResponseEntity<Page<MiembroResponseDto>> listarMiembrosPaginados(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio
    ) {
        Pageable pageable = PageRequest.of(pagina, tamanio);
        return ResponseEntity.ok(miembroService.listarMiembrosPaginados(pageable));
    }

    /**
     * TOTAL DE USUARIOS - PÚBLICO (landing page)
     */
    @GetMapping("/public/total")
    public ResponseEntity<TotalUsuariosResponseDto> totalUsuariosPublico() {
        return ResponseEntity.ok(miembroService.obtenerTotalUsuarios());
    }

    /**
     * NOMBRE DEL REPRESENTANTE DISTRITAL - PÚBLICO (landing page)
     */
    @GetMapping("/public/representante")
    public ResponseEntity<RepresentanteResponseDto> representantePublico() {
        return ResponseEntity.ok(miembroService.obtenerRepresentanteDistrital());
    }
}
