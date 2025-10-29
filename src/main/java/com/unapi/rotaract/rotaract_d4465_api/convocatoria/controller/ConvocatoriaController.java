package com.unapi.rotaract.rotaract_d4465_api.convocatoria.controller;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.service.ConvocatoriaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


}
