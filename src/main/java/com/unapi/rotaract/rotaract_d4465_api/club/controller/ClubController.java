package com.unapi.rotaract.rotaract_d4465_api.club.controller;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.club.service.ClubServiceImpl;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

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
    @GetMapping("/public/")
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

    /**
     * Recupera un club por su identificador.
     *
     * Devuelve los datos del
     * club si existe; en caso contrario la capa de servicio lanzará una excepción
     * que se modelará como una respuesta de error (por ejemplo 404 o 400 según el caso).
     *
     * @param id identificador del club (debe ser mayor que 0)
     * @return {@link ClubResponseDto} con los datos del club solicitado
     */
    @GetMapping("/public/{id}")
    @Operation(
        summary = "Obtener club por id",
        description = "Devuelve los datos del club identificado por su id. Requiere rol 'REPRESENTANTE DISTRITAL'."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Club encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ClubResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (id no válido)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Club no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ClubResponseDto> getClubById(
            @Parameter(description = "Identificador único del club", required = true)
            @Valid @PathVariable long id
    ){
        ClubResponseDto club = clubService.findById(id);
        return ResponseEntity.ok(club);
    }
}
