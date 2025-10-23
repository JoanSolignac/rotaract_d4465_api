package com.unapi.rotaract.rotaract_d4465_api.club.controller;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.club.service.ClubServiceImpl;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.util.Map;

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
     * Devuelve los datos del club si existe; en caso contrario la capa de servicio lanzará una excepción
     * que se modelará como una respuesta de error (por ejemplo 404 o 400 según el caso).
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

    @PostMapping("/")
    @PreAuthorize("hasRole('REPRESENTANTE DISTRITAL')")
    @Operation(summary = "Crear club", description = "Crea un nuevo club. Requiere rol 'REPRESENTANTE DISTRITAL'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Club creado exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ClubResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (datos del club incorrectos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (role insuficiente)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    /**
     * Endpoint para crear un nuevo club.
     *
     * El cuerpo de la petición debe contener los datos necesarios para crear el club.
     * La respuesta incluye un mensaje y el club creado. El endpoint está protegido y
     * sólo puede ser llamado por usuarios con el rol 'REPRESENTANTE DISTRITAL'.
     *
     * @param clubDto DTO con los datos del club a crear (se valida mediante {@link jakarta.validation.Valid})
     * @return ResponseEntity con un mensaje y la representación del club creado
     */
    public ResponseEntity<?> createClub(
            @RequestBody(
                    description = "Datos necesarios para crear un nuevo club",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubResponseDto.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ClubResponseDto clubDto
    ){
        ClubResponseDto createdClub = clubService.createClub(clubDto);
        return ResponseEntity.ok(Map.of("message", "Club creado exitosamente", "club", createdClub));
    }

    /**
     * Endpoint para actualizar campos de un club existente.
     *
     * Realiza una actualización parcial: sólo los campos presentes en el DTO serán aplicados.
     * Requiere rol 'PRESIDENTE' y valida que el usuario autenticado tenga permiso sobre el club.
     *
     * @param id identificador del club a actualizar
     * @param clubDto DTO con los campos a modificar
     * @return ResponseEntity con un mensaje y la representación del club actualizado
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Actualizar club", description = "Actualiza los datos de un club existente. Requiere rol 'PRESIDENTE'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Club actualizado exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ClubResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (id o datos del club incorrectos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (role insuficiente)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Club no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> updateClub(
            @Parameter(description = "Identificador único del club", required = true)
            @Valid @PathVariable long id,
            @RequestBody(
                    description = "Datos del club a actualizar",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubEditRequestDto.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ClubEditRequestDto clubDto
    ){
        ClubResponseDto updatedClub = clubService.updateClub(id, clubDto);
        return ResponseEntity.ok(Map.of("message", "Club actualizado exitosamente", "club", updatedClub));
    }

    /**
     * Desactiva (marca como inactivo) el club identificado por {@code id}.
     *
     * Esta operación no elimina la entidad, sólo cambia su estado a inactivo. Requiere
     * rol 'REPRESENTANTE DISTRITAL' y validación de permisos en la capa de servicio.
     *
     * @param id identificador único del club
     * @return ResponseEntity con un mensaje de confirmación
     */
    @PatchMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('REPRESENTANTE DISTRITAL')")
    @Operation(summary = "Desactivar club", description = "Marca un club como inactivo. Requiere rol 'REPRESENTANTE DISTRITAL'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Club desactivado exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (id no válido)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (role insuficiente)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Club no encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> deactivateClub(
            @Parameter(description = "Identificador único del club", required = true)
            @Valid @PathVariable long id
    ){
        clubService.desactivateClub(id);
        return ResponseEntity.ok(Map.of("message", "Club desactivado exitosamente"));
    }

}
