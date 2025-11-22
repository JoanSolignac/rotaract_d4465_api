package com.unapi.rotaract.rotaract_d4465_api.convocatoria.controller;

import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces.IConvocatoriaService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones relacionadas con convocatorias.
 * Contiene endpoints públicos, para interesados y para presidentes.
 */
@RestController
@RequestMapping("/convocatorias")
@RequiredArgsConstructor
@Tag(name = "Convocatorias", description = "Operaciones para consultar y gestionar convocatorias")
public class ConvocatoriaController {

    private final IConvocatoriaService convocatoriaService;
    private final IInscripcionService inscripcionService;

    // =====================================================================
    // LISTAR CONVOCATORIAS (PÚBLICO)
    // =====================================================================

    @GetMapping("/public")
    @Operation(
            summary = "Listar convocatorias públicas",
            description = "Devuelve una página de convocatorias visibles públicamente."
    )
    public ResponseEntity<Page<ConvocatoriaResponseDto>> listarConvocatoriasPublicas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(convocatoriaService.findAll(page, size));
    }

    // =====================================================================
    // DISPONIBLES PARA INTERESADO
    // =====================================================================

    @GetMapping("/public/disponibles")
    @PreAuthorize("hasRole('INTERESADO')")
    @Operation(
            summary = "Listar convocatorias disponibles",
            description = "Devuelve las convocatorias activas en las que el usuario no está inscrito."
    )
    public ResponseEntity<Page<ConvocatoriaResponseDto>> listarConvocatoriasDisponibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(convocatoriaService.findDisponiblesParaInteresado(page, size));
    }

    // =====================================================================
    // LISTAR DEL PRESIDENTE
    // =====================================================================

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Listar convocatorias del presidente",
            description = "Devuelve una página de convocatorias asociadas al club del presidente autenticado."
    )
    public ResponseEntity<Page<ConvocatoriaResponseDto>> listarConvocatoriasPresidente(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(convocatoriaService.findAllByPresidente(page, size));
    }

    // =====================================================================
    // OBTENER POR ID (PÚBLICO)
    // =====================================================================

    @GetMapping("/public/{id}")
    @Operation(
            summary = "Obtener convocatoria por ID",
            description = "Devuelve información pública de una convocatoria."
    )
    public ResponseEntity<ConvocatoriaResponseDto> obtenerConvocatoriaPublica(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(convocatoriaService.findById(id));
    }

    // =====================================================================
    // CREAR CONVOCATORIA (PRESIDENTE)
    // =====================================================================

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Crear convocatoria",
            description = "Registra una nueva convocatoria. Solo disponible para PRESIDENTE."
    )
    public ResponseEntity<ConvocatoriaResponseDto> crearConvocatoria(
            @Valid @RequestBody ConvocatoriaCreateRequestDto dto
    ) {
        return ResponseEntity.ok(convocatoriaService.create(dto));
    }

    // =====================================================================
    // EDITAR CONVOCATORIA (PRESIDENTE)
    // =====================================================================

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(
            summary = "Editar convocatoria",
            description = "Actualiza parcialmente una convocatoria."
    )
    public ResponseEntity<ConvocatoriaResponseDto> editarConvocatoria(
            @PathVariable Long id,
            @Valid @RequestBody ConvocatoriaEditRequestDto dto
    ) {
        return ResponseEntity.ok(convocatoriaService.update(id, dto));
    }

    // =====================================================================
    // INSCRIBIRSE COMO INTERESADO
    // =====================================================================

    @PostMapping("/{convocatoriaId}/inscribirse")
    @PreAuthorize("hasRole('INTERESADO')")
    @Operation(
            summary = "Inscribirse a convocatoria",
            description = "Registra la inscripción del usuario autenticado."
    )
    public ResponseEntity<String> inscribirseEnConvocatoria(
            @PathVariable Long convocatoriaId
    ) {
        inscripcionService.inscribirseEnConvocatoria(convocatoriaId);
        return ResponseEntity.ok("Inscripción registrada correctamente.");
    }

    // =====================================================================
    // LISTAR INSCRIPCIONES DE UNA CONVOCATORIA
    // =====================================================================

    @GetMapping("/{convocatoriaId}/inscripciones")
    @PreAuthorize("hasAnyRole('PRESIDENTE','REPRESENTANTE DISTRITAL')")
    @Operation(
            summary = "Listar inscripciones de una convocatoria",
            description = "Devuelve una página de inscripciones asociadas a la convocatoria."
    )
    public ResponseEntity<Page<InscripcionResponseDto>> listarInscripcionesConvocatoria(
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
    @Operation(summary = "Aceptar inscripción")
    public ResponseEntity<String> aceptarInscripcion(
            @PathVariable Long convocatoriaId,
            @PathVariable Long inscripcionId
    ) {
        inscripcionService.aceptarInscripcion(inscripcionId);
        return ResponseEntity.ok("Inscripción aceptada correctamente.");
    }

    // =====================================================================
    // RECHAZAR INSCRIPCIÓN
    // =====================================================================

    @PostMapping("/{convocatoriaId}/inscripciones/{inscripcionId}/rechazar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Rechazar inscripción")
    public ResponseEntity<String> rechazarInscripcion(
            @PathVariable Long convocatoriaId,
            @PathVariable Long inscripcionId
    ) {
        inscripcionService.rechazarInscripcion(inscripcionId);
        return ResponseEntity.ok("Inscripción rechazada correctamente.");
    }

    // =====================================================================
    // CANCELAR INSCRIPCIÓN DEL USUARIO (INTERESADO)
    // =====================================================================

    @DeleteMapping("/{convocatoriaId}/cancelar-inscripcion")
    @PreAuthorize("hasRole('INTERESADO')")
    @Operation(
            summary = "Cancelar mi inscripción a la convocatoria",
            description = "Permite al usuario autenticado cancelar su inscripción si está en estado PENDIENTE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción cancelada correctamente"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> cancelarInscripcionConvocatoria(
            @PathVariable Long convocatoriaId
    ) {
        inscripcionService.cancelarInscripcionConvocatoria(convocatoriaId);
        return ResponseEntity.ok("Inscripción cancelada correctamente.");
    }
}
