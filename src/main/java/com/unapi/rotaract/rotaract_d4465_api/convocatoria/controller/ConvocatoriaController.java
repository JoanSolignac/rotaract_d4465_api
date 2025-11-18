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

/**
 * Controlador REST para operaciones relacionadas con convocatorias.
 * Expone endpoints públicos y privados según el rol del usuario.
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
            description = "Devuelve una página de convocatorias accesibles públicamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<Page<ConvocatoriaResponseDto>> listarConvocatorias(
            @Valid @RequestParam(defaultValue = "0")
            @Parameter(description = "Número de página (0-based)") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Cantidad de resultados por página") int size
    ) {
        return ResponseEntity.ok(convocatoriaService.findAll(page, size));
    }

    // =====================================================================
    // LISTAR CONVOCATORIAS DEL PRESIDENTE
    // =====================================================================

    @GetMapping
    @Operation(
            summary = "Listar convocatorias del presidente",
            description = "Devuelve una página de convocatorias del club del presidente autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<Page<ConvocatoriaResponseDto>> listarConvocatoriasPresidente(
            @Valid @RequestParam(defaultValue = "0")
            @Parameter(description = "Número de página (0-based)") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Cantidad de resultados por página") int size
    ) {
        return ResponseEntity.ok(convocatoriaService.findAllByPresidente(page, size));
    }

    // =====================================================================
    // OBTENER POR ID (PÚBLICO)
    // =====================================================================

    @GetMapping("/public/{id}")
    @Operation(
            summary = "Obtener convocatoria pública por ID",
            description = "Devuelve los datos de una convocatoria accesible públicamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convocatoria encontrada",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Convocatoria no encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> obtenerConvocatoria(
            @Parameter(description = "Identificador único de la convocatoria", required = true)
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convocatoria creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> crearConvocatoria(
            @Valid @RequestBody
            @Parameter(description = "Datos para la creación de la convocatoria", required = true)
            ConvocatoriaCreateRequestDto dto
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
            description = "Actualiza parcialmente los campos de una convocatoria existente. Solo PRESIDENTE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convocatoria actualizada",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Convocatoria no encontrada",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> editarConvocatoria(
            @Parameter(description = "ID de la convocatoria a editar", required = true)
            @PathVariable Long id,

            @Valid @RequestBody
            @Parameter(description = "Datos a modificar en la convocatoria") ConvocatoriaEditRequestDto dto
    ) {
        return ResponseEntity.ok(convocatoriaService.update(id, dto));
    }

    // =====================================================================
    // INSCRIBIRSE (INTERESADO)
    // =====================================================================

    @PostMapping("/{convocatoriaId}/inscribirse")
    @PreAuthorize("hasRole('INTERESADO')")
    @Operation(
            summary = "Inscribirse en una convocatoria",
            description = "Registra la inscripción del usuario autenticado en la convocatoria seleccionada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Error al registrar la inscripción",
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
    // LISTAR INSCRIPCIONES (PRESIDENTE / REPRESENTANTE)
    // =====================================================================

    @GetMapping("/{convocatoriaId}/inscripciones")
    @PreAuthorize("hasAnyRole('PRESIDENTE','REPRESENTANTE DISTRITAL')")
    @Operation(
            summary = "Listar inscripciones de una convocatoria",
            description = "Devuelve una página con las inscripciones asociadas a la convocatoria seleccionada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
    })
    public ResponseEntity<Page<InscripcionResponseDto>> listarInscripcionesConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long convocatoriaId,

            @RequestParam(defaultValue = "0")
            @Parameter(description = "Página solicitada") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Tamaño de página") int size
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
            description = "Aprueba una inscripción asociada a la convocatoria seleccionada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción aceptada correctamente")
    })
    public ResponseEntity<String> aceptarInscripcionConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long convocatoriaId,

            @Parameter(description = "ID de la inscripción a aprobar", required = true)
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
            description = "Rechaza una inscripción asociada a la convocatoria seleccionada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción rechazada correctamente")
    })
    public ResponseEntity<String> rechazarInscripcionConvocatoria(
            @Parameter(description = "ID de la convocatoria", required = true)
            @PathVariable Long convocatoriaId,

            @Parameter(description = "ID de la inscripción a rechazar", required = true)
            @PathVariable Long inscripcionId
    ) {
        inscripcionService.rechazarInscripcion(inscripcionId);
        return ResponseEntity.ok("La inscripción ha sido rechazada correctamente.");
    }
}
