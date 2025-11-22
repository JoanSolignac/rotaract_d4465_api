package com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record ProyectoResponseDto(

        Long id,
        String titulo,
        String descripcion,
        String objetivo,
        String requisitos,
        String lugar,

        LocalDate fechaInicioPostulacion,
        LocalDate fechaFinPostulacion,
        LocalDate fechaInicioProyecto,
        LocalDate fechaFinProyecto,

        Long clubId,
        String clubNombre,

        String estadoProyecto,

        Integer cupoMaximo,
        Integer inscritos,

        /**
         * NUEVO: indica si el usuario actual puede inscribirse.
         * (true = disponible para inscripción)
         */
        Boolean disponible

) {}
