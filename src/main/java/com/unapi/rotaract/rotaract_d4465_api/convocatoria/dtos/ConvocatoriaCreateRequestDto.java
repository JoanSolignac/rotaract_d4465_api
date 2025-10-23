package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder

public record ConvocatoriaCreateRequestDto(
        @NotNull(message = "El clubId es obligatorio.")
        Long clubId,
        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 155, message = "El título no debe exceder 155 caracteres.")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres.")
        String descripcion,

        @NotNull(message = "La fecha de inicio es obligatoria.")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria.")
        LocalDate fechaFin,

        @NotBlank(message = "El lugar es obligatorio.")
        @Size(max = 155, message = "El lugar no debe exceder 155 caracteres.")
        String lugar,

        @NotNull(message = "La capacidad es obligatoria.")
        @Positive(message = "La capacidad debe ser mayor que 0.")
        Integer capacidad,

        @NotNull(message = "El inicio de inscripción es obligatorio.")
        LocalDate inicioInscripcion,

        @NotNull(message = "El fin de inscripción es obligatorio.")
        LocalDate finInscripcion
) {
}
