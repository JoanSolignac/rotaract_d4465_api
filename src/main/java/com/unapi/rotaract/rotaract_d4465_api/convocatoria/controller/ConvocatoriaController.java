package com.unapi.rotaract.rotaract_d4465_api.convocatoria.controller;

import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces.IConvocatoriaService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    private final IConvocatoriaService convocatoriaService;
    private final IInscripcionService inscripcionService;

    // =====================================================================
    // LISTAR CONVOCATORIAS
    // =====================================================================

    @GetMapping("/public")
    @Operation(
            summary = "Listar convocatorias",
            description = "Devuelve una lista paginada de convocatorias accesibles públicamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados obtenidos",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<Page<ConvocatoriaResponseDto>> listarConvocatorias(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(convocatoriaService.findAll(page, size));
    }

    // =====================================================================
    // OBTENER POR ID
    // =====================================================================

    @GetMapping("/public/{id}")
    @Operation(
            summary = "Obtener convocatoria por ID",
            description = "Devuelve los datos de una convocatoria pública."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convocatoria encontrada",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> obtenerConvocatoria(
            @Parameter(description = "Identificador de la convocatoria", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(convocatoriaService.findById(id));
    }

    // =====================================================================
    // CREAR
    // =====================================================================

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Crear convocatoria",
            description = "Registra una nueva convocatoria en el sistema (solo PRESIDENTE)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convocatoria creada correctamente",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> crearConvocatoria(
            @Valid @RequestBody ConvocatoriaCreateRequestDto dto
    ) {
        return ResponseEntity.ok(convocatoriaService.create(dto));
    }

    // =====================================================================
    // EDITAR
    // =====================================================================

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Editar convocatoria",
            description = "Actualiza parcialmente los campos de una convocatoria existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convocatoria actualizada",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Convocatoria no encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> editarConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ConvocatoriaEditRequestDto dto
    ) {
        return ResponseEntity.ok(convocatoriaService.update(id, dto));
    }

    // =====================================================================
    // INSCRIBIRSE
    // =====================================================================

    @PostMapping("/{convocatoriaId}/inscribirse")
    @PreAuthorize("hasRole('INTERESADO')")
    @Operation(
            summary = "Inscribirse en una convocatoria",
            description = "Registra la inscripción del usuario autenticado en la convocatoria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "No se pudo registrar la inscripción",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<String> inscribirseEnConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long convocatoriaId
    ) {
        inscripcionService.inscribirseEnConvocatoria(convocatoriaId);
        return ResponseEntity.ok("La inscripción se ha registrado correctamente.");
    }

    // =====================================================================
    // LISTAR INSCRIPCIONES
    // =====================================================================

    @GetMapping("/{convocatoriaId}/inscripciones")
    @PreAuthorize("hasAnyRole('PRESIDENTE','REPRESENTANTE DISTRITAL')")
    @Operation(
            summary = "Listar inscripciones",
            description = "Devuelve una página con todas las inscripciones asociadas a la convocatoria."
    )
    public ResponseEntity<Page<InscripcionResponseDto>> listarInscripcionesConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long convocatoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                inscripcionService.listarInscripcionesConvocatoria(convocatoriaId, page, size)
        );
    }

    // =====================================================================
    // ACEPTAR INSCRIPCIÓN
    // =====================================================================

    @PostMapping("/{convocatoriaId}/inscripciones/{inscripcionId}/aceptar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Aceptar inscripción",
            description = "Aprueba una inscripción asociada a la convocatoria."
    )
    public ResponseEntity<String> aceptarInscripcionConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long convocatoriaId,
            @Parameter(description = "ID de la inscripción", required = true)
            @PathVariable Long inscripcionId
    ) {
        inscripcionService.aceptarInscripcion(inscripcionId);
        return ResponseEntity.ok("La inscripción ha sido aceptada correctamente.");
    }

    // =====================================================================
    // RECHAZAR INSCRIPCIÓN
    // =====================================================================

    @PostMapping("/{convocatoriaId}/inscripciones/{inscripcionId}/rechazar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Rechazar inscripción",
            description = "Rechaza una inscripción asociada a la convocatoria."
    )
    public ResponseEntity<String> rechazarInscripcionConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long convocatoriaId,
            @Parameter(description = "ID de la inscripción", required = true)
            @PathVariable Long inscripcionId
    ) {
        inscripcionService.rechazarInscripcion(inscripcionId);
        return ResponseEntity.ok("La inscripción ha sido rechazada correctamente.");
    }
}
