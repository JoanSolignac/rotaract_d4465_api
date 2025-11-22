package com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos;

import lombok.Builder;

/**
 * DTO para exponer la asistencia de un usuario dentro de un proyecto.
 */
@Builder
public record AsistenciaResponseDto(

        Long usuarioId,
        String usuarioNombreCompleto,
        String usuarioCorreo,

        Long proyectoId,

        boolean presente
) {}
