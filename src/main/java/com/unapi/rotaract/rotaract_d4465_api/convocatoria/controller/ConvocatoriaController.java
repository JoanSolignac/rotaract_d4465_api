package com.unapi.rotaract.rotaract_d4465_api.convocatoria.controller;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.service.ConvocatoriaServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/convocatorias")
@RequiredArgsConstructor
@Tag(name = "Convocatorias", description = "Operaciones para consultar y gestionar convocatorias")
public class ConvocatoriaController {

    private final ConvocatoriaServiceImpl convocatoriaService;

    @GetMapping("/public/")
    public ResponseEntity<Page<ConvocatoriaResponseDto>> getAllConvocatorias(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ConvocatoriaResponseDto> ConvocatoriasPage = convocatoriaService.findAll(page, size);
        return ResponseEntity.ok(ConvocatoriasPage);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ConvocatoriaResponseDto> getConvocatoriaById(@PathVariable Long id) {
        ConvocatoriaResponseDto convocatoria = convocatoriaService.findById(id);
        return ResponseEntity.ok(convocatoria);
    }

    @PostMapping("/" )
    @PreAuthorize("hasRole('PRESIDENTE')")
    public ResponseEntity<ConvocatoriaResponseDto> createConvocatoria(
            @Valid @RequestBody ConvocatoriaCreateRequestDto convocatoriaCreateRequestDto
    ) {
        ConvocatoriaResponseDto createdConvocatoria = convocatoriaService.create(convocatoriaCreateRequestDto);
        return ResponseEntity.ok(createdConvocatoria);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    public ResponseEntity<ConvocatoriaResponseDto> updateConvocatoria(
            @PathVariable Long id,
            @Valid @RequestBody ConvocatoriaEditRequestDto convocatoriaEditRequestDto
    ) {
        ConvocatoriaResponseDto updatedConvocatoria = convocatoriaService.update(id, convocatoriaEditRequestDto);
        return ResponseEntity.ok(updatedConvocatoria);
    }
}
