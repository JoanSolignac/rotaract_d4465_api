package com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Datos necesarios para registrar un nuevo proyecto.
 * Incluye fechas de postulación, fechas de ejecución y atributos descriptivos.
 * Garantiza presencia y formato mediante validaciones.
 */
public record ProyectoCreateRequestDto(

        @NotBlank(message = "El título es obligatorio.")
        String titulo,

        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres.")
        String descripcion,

        @NotBlank(message = "El objetivo es obligatorio.")
        String objetivo,

        @NotNull(message = "La fecha de inicio de postulación es obligatoria.")
        LocalDate fechaInicioPostulacion,

        @NotNull(message = "La fecha de fin de postulación es obligatoria.")
        LocalDate fechaFinPostulacion,

        @NotNull(message = "La fecha de inicio del proyecto es obligatoria.")
        LocalDate fechaInicioProyecto,

        @NotNull(message = "La fecha de fin del proyecto es obligatoria.")
        LocalDate fechaFinProyecto,

        @NotBlank(message = "El lugar es obligatorio.")
        String lugar,

        @Positive(message = "El cupo máximo no puede ser menor a cero")
        Integer cupoMaximo,

        @Size(max = 500, message = "Los requisitos no deben exceder 500 caracteres.")
        String requisitos

) {}
