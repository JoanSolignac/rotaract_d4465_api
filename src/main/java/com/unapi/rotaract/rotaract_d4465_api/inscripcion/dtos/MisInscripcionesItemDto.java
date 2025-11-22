package com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO que representa una inscripción del usuario autenticado,
 * unificando convocatorias y proyectos en una sola estructura.
 */
@Schema(description = "Elemento de la lista de inscripciones del usuario autenticado")
public record MisInscripcionesItemDto(

        @Schema(description = "ID de la inscripción")
        Long inscripcionId,

        @Schema(description = "Tipo del evento asociado (CONVOCATORIA o PROYECTO)")
        String tipoEvento,

        @Schema(description = "ID del evento asociado (convocatoria o proyecto)")
        Long eventoId,

        @Schema(description = "Título del evento (convocatoria o proyecto)")
        String tituloEvento,

        @Schema(description = "Nombre del club organizador")
        String clubNombre,

        @Schema(description = "Estado actual de la inscripción")
        String estadoInscripcion,

        @Schema(description = "Fecha y hora en la que se registró la inscripción")
        LocalDateTime fechaRegistro

) {}
