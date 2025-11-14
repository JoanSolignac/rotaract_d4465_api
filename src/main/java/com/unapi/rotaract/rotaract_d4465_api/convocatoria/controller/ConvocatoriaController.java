package com.unapi.rotaract.rotaract_d4465_api.convocatoria.controller;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.service.ConvocatoriaServiceImpl;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * Controlador REST para operaciones relacionadas con convocatorias.
 * Expone endpoints públicos bajo el prefijo {@code /convocatorias} y endpoints protegidos para creación y actualización.
 *
 * <p>Actualmente permite:
 * <ul>
 *   <li>Listar convocatorias de forma paginada.</li>
 *   <li>Obtener una convocatoria por su identificador.</li>
 *   <li>Crear una nueva convocatoria (rol requerido: PRESIDENTE).</li>
 *   <li>Actualizar parcialmente una convocatoria existente (rol requerido: PRESIDENTE).</li>
 * </ul>
 *
 * Los errores se modelan con {@link com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto}.
 */
@RestController
@RequestMapping("/convocatorias")
@RequiredArgsConstructor
@Tag(name = "Convocatorias", description = "Operaciones para consultar y gestionar convocatorias")
public class ConvocatoriaController {

    private final ConvocatoriaServiceImpl convocatoriaService;

    /**
     * Devuelve una página de convocatorias.
     *
     * @param page índice de página (0-based), por defecto 0
     * @param size tamaño de página, por defecto 10
     * @return página con {@link ConvocatoriaResponseDto} y metadatos de paginación
     */
    @GetMapping("/public/")
    @Operation(
        summary = "Listar convocatorias",
        description = "Devuelve una página con las convocatorias registradas. Endpoint público."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado paginado de convocatorias",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (parámetros no válidos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<Page<ConvocatoriaResponseDto>> getAllConvocatorias(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ConvocatoriaResponseDto> convocatoriasPage = convocatoriaService.findAll(page, size);
        return ResponseEntity.ok(convocatoriasPage);
    }

    /**
     * Recupera una convocatoria por su identificador.
     * Devuelve los datos si existe; de lo contrario la capa de servicio lanzará una excepción
     * que se traducirá en una respuesta de error (404 o 400 según corresponda).
     *
     * @param id identificador de la convocatoria
     * @return {@link ConvocatoriaResponseDto} con los datos de la convocatoria
     */
    @GetMapping("/public/{id}")
    @Operation(
        summary = "Obtener convocatoria por id",
        description = "Devuelve los datos de la convocatoria identificada por su id. Endpoint público."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Convocatoria encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (id no válido)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Convocatoria no encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> getConvocatoriaById(
            @Parameter(description = "Identificador único de la convocatoria", required = true)
            @PathVariable Long id) {
        ConvocatoriaResponseDto convocatoria = convocatoriaService.findById(id);
        return ResponseEntity.ok(convocatoria);
    }

    /**
     * Crea una nueva convocatoria.
     * Operación protegida: requiere rol 'PRESIDENTE'.
     *
     * @param convocatoriaCreateRequestDto DTO con los datos necesarios para crear la convocatoria
     * @return {@link ConvocatoriaResponseDto} con la convocatoria creada
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Crear convocatoria", description = "Crea una nueva convocatoria. Requiere rol 'PRESIDENTE'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Convocatoria creada exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (datos incorrectos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (rol insuficiente)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> createConvocatoria(
            @RequestBody(
                description = "Datos necesarios para crear una nueva convocatoria",
                required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConvocatoriaCreateRequestDto.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ConvocatoriaCreateRequestDto convocatoriaCreateRequestDto
    ) {
        ConvocatoriaResponseDto createdConvocatoria = convocatoriaService.create(convocatoriaCreateRequestDto);
        return ResponseEntity.ok(createdConvocatoria);
    }

    /**
     * Actualiza parcialmente una convocatoria existente.
     * Sólo se aplican los campos presentes en el DTO. Requiere rol 'PRESIDENTE'.
     *
     * @param id identificador de la convocatoria a actualizar
     * @param convocatoriaEditRequestDto DTO con los campos a modificar
     * @return {@link ConvocatoriaResponseDto} con la convocatoria actualizada
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENTE')")
    @Operation(summary = "Actualizar convocatoria", description = "Actualiza los datos de una convocatoria existente. Requiere rol 'PRESIDENTE'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Convocatoria actualizada exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ConvocatoriaResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (id o datos incorrectos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (rol insuficiente)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Convocatoria no encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<ConvocatoriaResponseDto> updateConvocatoria(
            @Parameter(description = "Identificador único de la convocatoria", required = true)
            @PathVariable Long id,
            @RequestBody(
                description = "Campos de la convocatoria a actualizar",
                required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConvocatoriaEditRequestDto.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ConvocatoriaEditRequestDto convocatoriaEditRequestDto
    ) {
        ConvocatoriaResponseDto updatedConvocatoria = convocatoriaService.update(id, convocatoriaEditRequestDto);
        return ResponseEntity.ok(updatedConvocatoria);
    }
}
