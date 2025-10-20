package com.unapi.rotaract.rotaract_d4465_api.club.controller;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.club.service.ClubServiceImpl;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para operaciones relacionadas con clubes.
 * Expone endpoints públicos bajo el prefijo {@code /clubs}.
 *
 * <p>Actualmente provee operaciones de consulta (paginadas) de clubes. Los errores
 * se modelan con {@link com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto}.
 */
@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubes", description = "Operaciones para consultar y gestionar clubes")
public class ClubController {

    private final ClubServiceImpl clubService;

    /**
     * Devuelve una página de clubes.
     *
     * @param page índice de página (0-based), por defecto 0
     * @param size tamaño de página, por defecto 10
     * @return página con {@link ClubResponseDto} y metadatos de paginación
     */
    @PreAuthorize("hasRole('REPRESENTANTE DISTRITAL')")
    @GetMapping()
    @Operation(
        summary = "Listar clubes",
        description = "Devuelve una página con los clubes registrados. Requiere rol 'REPRESENTANTE DISTRITAL'."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado paginado de clubes",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ClubResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (parámetros no válidos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<Page<ClubResponseDto>> getAllClubs(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ClubResponseDto> clubsPage = clubService.findAll(page, size);
        return ResponseEntity.ok(clubsPage);
    }
}
