package com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos;

import java.time.LocalDateTime;

/**
 * Representación simplificada de una inscripción
 * utilizada para respuestas de API.
 */
public record InscripcionResponseDto(

        Long id,

        Long usuarioId,
        String usuarioNombre,
        String usuarioCorreo,

        String tipo,             // "CONVOCATORIA" o "PROYECTO"
        Long referenciaId,       // id de la convocatoria o proyecto
        String referenciaTitulo, // título de la convocatoria o proyecto

        String estado,
        LocalDateTime fechaRegistro

) {}
