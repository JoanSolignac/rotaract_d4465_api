package com.unapi.rotaract.rotaract_d4465_api.club.controller;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.*;
import com.unapi.rotaract.rotaract_d4465_api.club.interfaces.IClubConsultaService;
import com.unapi.rotaract.rotaract_d4465_api.club.service.ClubServiceImpl;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubes", description = "Operaciones para consultar y gestionar clubes del distrito")
public class ClubController {

    private final ClubServiceImpl clubService;
    private final IClubConsultaService clubConsultaService;

    // -------------------------------------------------------------------------
    // GET: Listar clubes
    // -------------------------------------------------------------------------
    @GetMapping("/public")
    @Operation(summary = "Listar clubes", description = "Devuelve una página con los clubes registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado paginado de clubes",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClubResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<Page<ClubResponseDto>> getAllClubs(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(clubService.findAll(page, size));
    }

    // -------------------------------------------------------------------------
    // GET: Obtener club por id
    // -------------------------------------------------------------------------
    @GetMapping("/public/{id}")
    @Operation(summary = "Obtener club por ID", description = "Devuelve la información del club solicitado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club encontrado",
                    content = @Content(schema = @Schema(implementation = ClubResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Club no encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ClubResponseDto> getClubById(
            @Parameter(description = "ID del club", required = true)
            @Valid @PathVariable long id
    ){
        return ResponseEntity.ok(clubService.findById(id));
    }

    // -------------------------------------------------------------------------
    // GET: Detalle completo de un club (nuevo)
    // -------------------------------------------------------------------------
    @GetMapping("/public/{id}/detalle")
    @Operation(
            summary = "Obtener detalle completo de un club",
            description = "Devuelve el club, presidente, integrantes paginados, convocatorias y proyectos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle del club obtenido",
                    content = @Content(schema = @Schema(implementation = ClubDetalleResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Club no encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ClubDetalleResponseDto> getClubDetalle(
            @Parameter(description = "ID del club", required = true)
            @PathVariable Long id,
            @Parameter(description = "Número de página para integrantes", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página para integrantes", example = "10")
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                clubConsultaService.obtenerDetalleClub(id, page, size)
        );
    }

    // -------------------------------------------------------------------------
    // GET: Métricas del club del presidente autenticado
    // -------------------------------------------------------------------------
    @GetMapping("/metricas")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Obtener métricas del club del presidente",
            description = "Devuelve las métricas y detalles completos del club del presidente autenticado, incluyendo convocatorias, proyectos e integrantes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas del club obtenidas",
                    content = @Content(schema = @Schema(implementation = ClubDetalleResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado - usuario no es presidente",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Presidente sin club asignado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ClubDetalleResponseDto> getMetricasClubPresidente(
            @Parameter(description = "Número de página para integrantes", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página para integrantes", example = "10")
            @RequestParam(defaultValue = "10") int size
    ){
        String correoPresidente = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                clubConsultaService.obtenerMetricasClubPresidente(correoPresidente, page, size)
        );
    }

    // -------------------------------------------------------------------------
    // POST: Crear club (sin presidente)
    // -------------------------------------------------------------------------
    @PostMapping("/")
    @PreAuthorize("hasRole('REPRESENTANTE DISTRITAL')")
    @Operation(summary = "Crear club", description = "Crea un nuevo club sin asignar presidente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club creado",
                    content = @Content(schema = @Schema(implementation = ClubResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> createClub(
            @Valid @RequestBody ClubCreateRequestDto clubDto
    ){
        ClubResponseDto createdClub = clubService.createClub(clubDto);
        return ResponseEntity.ok(Map.of(
                "message", "Club creado exitosamente",
                "club", createdClub
        ));
    }

    // -------------------------------------------------------------------------
    // POST: Crear club asignando presidente
    // -------------------------------------------------------------------------
    @PostMapping("/con-presidente")
    @PreAuthorize("hasRole('REPRESENTANTE DISTRITAL')")
    @Operation(
            summary = "Crear club asignando presidente",
            description = "Crea un club y asigna como presidente a un usuario existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club creado y presidente asignado",
                    content = @Content(schema = @Schema(implementation = ClubResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuario presidente no encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> createClubWithPresidente(
            @Valid @RequestBody ClubCreateWithPresidenteRequestDto dto
    ){
        ClubResponseDto created = clubService.createClubWithPresidente(dto);
        return ResponseEntity.ok(Map.of(
                "message", "Club creado y presidente asignado correctamente",
                "club", created
        ));
    }

    // -------------------------------------------------------------------------
    // PATCH: Actualizar club (solo presidente)
    // -------------------------------------------------------------------------
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Actualizar club", description = "Actualiza los datos del club. Solo para presidentes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club actualizado",
                    content = @Content(schema = @Schema(implementation = ClubResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> updateClub(
            @Valid @PathVariable long id,
            @Valid @RequestBody ClubEditRequestDto clubDto
    ){
        ClubResponseDto updated = clubService.updateClub(id, clubDto);
        return ResponseEntity.ok(Map.of(
                "message", "Club actualizado exitosamente",
                "club", updated
        ));
    }

    // -------------------------------------------------------------------------
    // PATCH: Desactivar club (solo representante distrital)
    // -------------------------------------------------------------------------
    @PatchMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('REPRESENTANTE DISTRITAL')")
    @Operation(
            summary = "Desactivar club",
            description = "Marca un club como inactivo. No lo elimina físicamente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club desactivado",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Club no encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> deactivateClub(
            @Valid @PathVariable long id
    ){
        clubService.desactivateClub(id);
        return ResponseEntity.ok(Map.of(
                "message", "Club desactivado exitosamente"
        ));
    }

    // -------------------------------------------------------------------------
    // DELETE: Eliminar socio
    // -------------------------------------------------------------------------
    @DeleteMapping("/{clubId}/socios/{socioId}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Eliminar socio del club",
            description = "Permite que el presidente elimine a un socio del club y lo regrese al rol INTERESADO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Socio eliminado correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Club o socio no encontrado")
    })
    public ResponseEntity<?> removeSocio(
            @PathVariable Long clubId,
            @PathVariable Long socioId
    ){
        clubService.removeSocioFromClub(clubId, socioId);
        return ResponseEntity.ok(Map.of("message", "Socio eliminado del club exitosamente."));
    }

    // -------------------------------------------------------------------------
    // POST: Transferir presidencia
    // ------------------------------------------------------------------------
    @PostMapping("/{clubId}/transferir-presidencia/{nuevoPresidenteId}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Transferir presidencia", description = "Permite al presidente transferir su cargo a otro socio del club.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presidencia transferida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuario o club no encontrado")
    })
    public ResponseEntity<?> transferirPresidencia(
            @PathVariable Long clubId,
            @PathVariable Long nuevoPresidenteId
    ){
        clubService.transferirPresidencia(clubId, nuevoPresidenteId);
        return ResponseEntity.ok(Map.of("message", "Presidencia transferida exitosamente."));
    }

}
