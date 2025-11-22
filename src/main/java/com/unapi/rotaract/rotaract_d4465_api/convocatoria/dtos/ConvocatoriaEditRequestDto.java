package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para edición parcial de convocatorias.
 * El servicio aplica validaciones de coherencia entre fechas.
 */
public record ConvocatoriaEditRequestDto(

        @Size(max = 120, message = "El título no debe exceder 120 caracteres.")
        String titulo,

        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres.")
        String descripcion,

        @Positive(message = "El cupo máximo debe ser un número positivo.")
        Integer cupoMaximo,

        LocalDate fechaInicioPostulacion,

        LocalDate fechaFinPostulacion,

        @Size(max = 500, message = "Los requisitos no deben exceder 500 caracteres.")
        String requisitos
) {}
