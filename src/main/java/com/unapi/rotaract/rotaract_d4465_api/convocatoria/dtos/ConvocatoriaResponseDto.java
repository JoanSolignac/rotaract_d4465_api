package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import java.time.LocalDate;

/**
 * DTO de salida para convocatorias.
 */
public record ConvocatoriaResponseDto(
        Long id,
        String titulo,
        String descripcion,
        String requisitos,

        Integer cupoMaximo,
        Integer inscritos,
        LocalDate fechaPublicacion,
        LocalDate fechaCierre,

        LocalDate fechaInicioPostulacion,
        LocalDate fechaFinPostulacion,

        Long clubId,
        String clubNombre,

        String estado
) {}
