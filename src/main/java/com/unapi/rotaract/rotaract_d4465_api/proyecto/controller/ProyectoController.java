package com.unapi.rotaract.rotaract_d4465_api.proyecto.controller;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaGuardarRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.interfaces.IAsistenciaService;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.interfaces.IProyectoService;

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

import java.util.List;

/**
 * Controlador encargado de exponer operaciones públicas y protegidas
 * para la gestión completa de proyectos.
 */
@RestController
@RequestMapping("/proyectos")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Operaciones para consultar y gestionar proyectos")
public class ProyectoController {

    private final IProyectoService proyectoService;
    private final IInscripcionService inscripcionService;
    private final IAsistenciaService asistenciaService;

    // ============================================================
    // PUBLIC — LISTAR PROYECTOS
    // ============================================================

    @GetMapping("/public")
    @Operation(
            summary = "Listar proyectos",
            description = "Devuelve una lista paginada de todos los proyectos."
    )
    public ResponseEntity<Page<ProyectoResponseDto>> listarProyectos(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(proyectoService.findAll(page, size));
    }

    @GetMapping("/public/{id}")
    @Operation(
            summary = "Obtener proyecto",
            description = "Recupera los detalles de un proyecto por su identificador."
    )
    public ResponseEntity<ProyectoResponseDto> obtenerProyecto(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.findById(id));
    }

    @GetMapping("/public/buscar")
    @Operation(
            summary = "Buscar proyectos por título",
            description = "Busca proyectos por coincidencia en el título."
    )
    public ResponseEntity<Page<ProyectoResponseDto>> buscarPorTitulo(
            @RequestParam String titulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(proyectoService.buscarPorTitulo(titulo, page, size));
    }

    // ============================================================
    // LISTAR PROYECTOS DEL CLUB (SOCIO / PRESIDENTE)
    // ============================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIO','PRESIDENTE')")
    @Operation(
            summary = "Listar proyectos del club del usuario",
            description = "Devuelve una página de proyectos pertenecientes al club del socio o presidente autenticado."
    )
    public ResponseEntity<Page<ProyectoResponseDto>> listarProyectosPorClub(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(proyectoService.findAllBySocioPresidente(page, size));
    }

    // ============================================================
    // LISTAR PROYECTOS DISPONIBLES (NO INSCRITOS)
    // ============================================================

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('SOCIO','PRESIDENTE')")
    @Operation(
            summary = "Listar proyectos disponibles",
            description = "Devuelve solo los proyectos del club en los que el usuario NO está inscrito."
    )
    public ResponseEntity<Page<ProyectoResponseDto>> listarProyectosDisponibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(proyectoService.findDisponiblesParaUsuario(page, size));
    }

    // ============================================================
    // CREAR / EDITAR / CANCELAR / FINALIZAR
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Crear proyecto")
    public ResponseEntity<?> crearProyecto(@Valid @RequestBody ProyectoCreateRequestDto dto) {
        ProyectoResponseDto creado = proyectoService.create(dto);
        return ResponseEntity.ok("Proyecto creado correctamente con ID: " + creado.id());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Editar proyecto")
    public ResponseEntity<?> editarProyecto(
            @PathVariable Long id,
            @Valid @RequestBody ProyectoEditRequestDto dto) {

        ProyectoResponseDto actualizado = proyectoService.update(id, dto);
        return ResponseEntity.ok("Proyecto actualizado correctamente: " + actualizado.titulo());
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Cancelar proyecto")
    public ResponseEntity<?> cancelarProyecto(@PathVariable Long id) {
        proyectoService.cancelarProyecto(id);
        return ResponseEntity.ok("Proyecto cancelado correctamente.");
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Finalizar proyecto")
    public ResponseEntity<?> finalizarProyecto(@PathVariable Long id) {
        proyectoService.finalizarProyecto(id);
        return ResponseEntity.ok("Proyecto finalizado exitosamente.");
    }

    // ============================================================
    // INSCRIPCIONES
    // ============================================================

    @PostMapping("/{proyectoId}/inscribirse")
    @PreAuthorize("hasAnyRole('SOCIO','PRESIDENTE')")
    @Operation(summary = "Inscribirse en proyecto")
    public ResponseEntity<?> inscribirseEnProyecto(@PathVariable Long proyectoId) {
        inscripcionService.inscribirseEnProyecto(proyectoId);
        return ResponseEntity.ok("Inscripción registrada correctamente.");
    }

    @GetMapping("/{proyectoId}/inscripciones")
    @PreAuthorize("hasAnyRole('PRESIDENTE','REPRESENTANTE DISTRITAL','SOCIO')")
    @Operation(summary = "Listar inscripciones del proyecto")
    public ResponseEntity<Page<InscripcionResponseDto>> listarInscripcionesProyecto(
            @PathVariable Long proyectoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(inscripcionService.listarInscripcionesProyecto(proyectoId, page, size));
    }

    @PostMapping("/{proyectoId}/inscripciones/{inscripcionId}/aceptar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Aceptar inscripción")
    public ResponseEntity<?> aceptarInscripcionProyecto(
            @PathVariable Long proyectoId,
            @PathVariable Long inscripcionId) {

        inscripcionService.aceptarInscripcion(inscripcionId);
        return ResponseEntity.ok("La inscripción fue aceptada satisfactoriamente.");
    }

    @PostMapping("/{proyectoId}/inscripciones/{inscripcionId}/rechazar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Rechazar inscripción")
    public ResponseEntity<?> rechazarInscripcionProyecto(
            @PathVariable Long proyectoId,
            @PathVariable Long inscripcionId) {

        inscripcionService.rechazarInscripcion(inscripcionId);
        return ResponseEntity.ok("La inscripción fue rechazada correctamente.");
    }

    // ============================================================
    // CANCELAR INSCRIPCIÓN (USUARIO AUTENTICADO)
    // ============================================================

    @DeleteMapping("/{proyectoId}/cancelar-inscripcion")
    @PreAuthorize("hasAnyRole('SOCIO','PRESIDENTE')")
    @Operation(
            summary = "Cancelar mi inscripción al proyecto",
            description = "Permite al usuario autenticado cancelar su inscripción si aún está en estado PENDIENTE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción cancelada correctamente"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<?> cancelarMiInscripcion(@PathVariable Long proyectoId) {
        inscripcionService.cancelarInscripcionProyecto(proyectoId);
        return ResponseEntity.ok("Inscripción cancelada correctamente.");
    }

    // ============================================================
    // ASISTENCIAS
    // ============================================================

    @GetMapping("/{proyectoId}/asistencia/lista")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Listar socios para asistencia",
            description = "Devuelve la lista de socios inscritos al proyecto para marcar asistencia.")
    public ResponseEntity<List<AsistenciaResponseDto>> listarAsistencia(
            @PathVariable Long proyectoId) {

        return ResponseEntity.ok(asistenciaService.listarSociosParaAsistencia(proyectoId));
    }

    @PostMapping("/{proyectoId}/asistencia/guardar")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Guardar asistencia",
            description = "Registra la asistencia (presentes y faltas) de los socios inscritos en el proyecto.")
    public ResponseEntity<?> guardarAsistencia(
            @PathVariable Long proyectoId,
            @RequestBody AsistenciaGuardarRequestDto dto) {

        asistenciaService.guardarAsistencia(proyectoId, dto);
        return ResponseEntity.ok("Asistencia registrada correctamente.");
    }

    @GetMapping("/{proyectoId}/asistencias")
    @PreAuthorize("hasAnyRole('PRESIDENTE','SOCIO')")
    @Operation(
            summary = "Ver asistencias del proyecto",
            description = "Devuelve el listado de asistencias ya registradas para el proyecto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de asistencias recuperado correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AsistenciaResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                    content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<List<AsistenciaResponseDto>> listarAsistenciasProyecto(
            @PathVariable Long proyectoId) {

        return ResponseEntity.ok(asistenciaService.listarAsistenciasDeProyecto(proyectoId));
    }
}
