package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;

@Builder

public record ConvocatoriaCreateRequestDto(
        @NotBlank(message = "Los requisitos son obligatorios.")
        @Size(min = 5, max = 1000, message = "Entre 5 y 1000 caracteres.")
        String requisitos,

        @NotBlank(message = "El tipo de convocatoria es obligatorio.")
        @Size(min = 3, max = 100, message = "Entre 3 y 100 caracteres.")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-_/]+$",
                message = "Solo letras, espacios y guiones.")
        String tipo
) {
}
