package com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos;

import java.time.LocalDateTime;

public record HistorialAsistenciaDto(
        Long asistenciaId,
        Long proyectoId,
        String proyectoTitulo,
        String estado,
        LocalDateTime fechaRegistro
) {}
