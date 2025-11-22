package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para la creación de convocatorias.
 * Las fechas de publicación y cierre se toman automáticamente:
 *  fechaPublicacion = fechaInicioPostulacion
 *  fechaCierre      = fechaFinPostulacion
 */
public record ConvocatoriaCreateRequestDto(

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 120, message = "El título no debe exceder 120 caracteres.")
        String titulo,

        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres.")
        String descripcion,

        @NotNull(message = "El cupo máximo es obligatorio.")
        @Positive(message = "El cupo máximo debe ser un número positivo.")
        Integer cupoMaximo,

        @NotNull(message = "La fecha de inicio de postulación es obligatoria.")
        LocalDate fechaInicioPostulacion,

        @NotNull(message = "La fecha de fin de postulación es obligatoria.")
        LocalDate fechaFinPostulacion,

        @Size(max = 500, message = "Los requisitos no deben exceder 500 caracteres.")
        String requisitos
) {}
