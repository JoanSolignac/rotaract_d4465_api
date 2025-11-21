package com.unapi.rotaract.rotaract_d4465_api.proyecto.controller;

import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.interfaces.IProyectoService;

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
 * Controlador encargado de exponer operaciones públicas y protegidas para la
 * gestión completa de proyectos.
 */
@RestController
@RequestMapping("/proyectos")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Operaciones para consultar y gestionar proyectos")
public class ProyectoController {

    private final IProyectoService proyectoService;
    private final IInscripcionService inscripcionService;

    // ============================================================
    // LISTAR PROYECTOS
    // ============================================================

    @GetMapping("/public")
    @Operation(summary = "Listar proyectos", description = "Devuelve una lista paginada de todos los proyectos.")
    public ResponseEntity<Page<ProyectoResponseDto>> listarProyectos(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(proyectoService.findAll(page, size));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Obtener proyecto", description = "Recupera los detalles de un proyecto por su identificador.")
    public ResponseEntity<ProyectoResponseDto> obtenerProyecto(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.findById(id));
    }


    // ============================================================
    // LISTAR PROYECTOS POR PRESIDENTE - SOCIO
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('SOCIO') or hasRole('PRESIDENTE')")
    @Operation(
            summary = "Listar convocatorias del presidente y del Socio",
            description = "Devuelve una página de convocatorias del club del presidente y socio autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                    content = @Content(schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<Page<ProyectoResponseDto>> listarProyectosPresidente(
            @Valid @RequestParam(defaultValue = "0")
            @Parameter(description = "Número de página (0-based)") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Cantidad de resultados por página") int size
    ) {
        return ResponseEntity.ok(proyectoService.findAllBySocioPresidente(page, size));
    }

    // ============================================================
    // CREAR PROYECTO
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Crear proyecto", description = "Permite registrar un nuevo proyecto dentro del sistema.")
    public ResponseEntity<?> crearProyecto(@Valid @RequestBody ProyectoCreateRequestDto dto) {
        ProyectoResponseDto creado = proyectoService.create(dto);
        return ResponseEntity.ok("Proyecto creado correctamente con ID: " + creado.id());
    }

    // ============================================================
    // EDITAR PROYECTO
    // ============================================================

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Editar proyecto", description = "Actualiza parcialmente los datos de un proyecto existente.")
    public ResponseEntity<?> editarProyecto(
            @PathVariable Long id,
            @Valid @RequestBody ProyectoEditRequestDto dto
    ) {
        ProyectoResponseDto actualizado = proyectoService.update(id, dto);
        return ResponseEntity.ok("Proyecto actualizado correctamente: " + actualizado.titulo());
    }

    // ============================================================
    // CANCELAR PROYECTO
    // ============================================================

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Cancelar proyecto", description = "Cambia el estado del proyecto a cancelado.")
    public ResponseEntity<?> cancelarProyecto(@PathVariable Long id) {
        proyectoService.cancelarProyecto(id);
        return ResponseEntity.ok("El proyecto fue cancelado correctamente.");
    }

    // ============================================================
    // FINALIZAR PROYECTO
    // ============================================================

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Finalizar proyecto", description = "Finaliza el proyecto y cierra su ciclo de vida.")
    public ResponseEntity<?> finalizarProyecto(@PathVariable Long id) {
        proyectoService.finalizarProyecto(id);
        return ResponseEntity.ok("El proyecto fue finalizado exitosamente.");
    }

    // ============================================================
    // BUSCAR PROYECTOS
    // ============================================================

    @GetMapping("/public/buscar")
    @Operation(summary = "Buscar proyectos por título", description = "Busca proyectos por coincidencia en el título.")
    public ResponseEntity<Page<ProyectoResponseDto>> buscarPorTitulo(
            @RequestParam String titulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(proyectoService.buscarPorTitulo(titulo, page, size));
    }

    // ============================================================
    // INSCRIPCIONES
    // ============================================================

    @PostMapping("/{proyectoId}/inscribirse")
    @PreAuthorize("hasAnyRole('SOCIO','PRESIDENTE')")
    @Operation(summary = "Inscribirse en proyecto", description = "Registra al usuario autenticado en el proyecto.")
    public ResponseEntity<?> inscribirseEnProyecto(@PathVariable Long proyectoId) {
        inscripcionService.inscribirseEnProyecto(proyectoId);
        return ResponseEntity.ok("Inscripción registrada correctamente.");
    }

    @GetMapping("/{proyectoId}/inscripciones")
    @PreAuthorize("hasAnyRole('PRESIDENTE','REPRESENTANTE DISTRITAL', 'SOCIO')")
    @Operation(summary = "Listar inscripciones", description = "Lista todas las inscripciones registradas para un proyecto.")
    public ResponseEntity<Page<InscripcionResponseDto>> listarInscripcionesProyecto(
            @PathVariable Long proyectoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(inscripcionService.listarInscripcionesProyecto(proyectoId, page, size));
    }

    @PostMapping("/{proyectoId}/inscripciones/{inscripcionId}/aceptar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Aceptar inscripción", description = "Aprueba una inscripción existente.")
    public ResponseEntity<?> aceptarInscripcionProyecto(
            @PathVariable Long proyectoId,
            @PathVariable Long inscripcionId
    ) {
        inscripcionService.aceptarInscripcion(inscripcionId);
        return ResponseEntity.ok("La inscripción fue aceptada satisfactoriamente.");
    }

    @PostMapping("/{proyectoId}/inscripciones/{inscripcionId}/rechazar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Rechazar inscripción", description = "Rechaza una inscripción específica.")
    public ResponseEntity<?> rechazarInscripcionProyecto(
            @PathVariable Long proyectoId,
            @PathVariable Long inscripcionId
    ) {
        inscripcionService.rechazarInscripcion(inscripcionId);
        return ResponseEntity.ok("La inscripción fue rechazada correctamente.");
    }
}
