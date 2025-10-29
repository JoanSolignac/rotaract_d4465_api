package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder

public record ConvocatoriaEditRequestDto(
        @Size(min = 3, max = 155, message = "El nombre de la convocatoria debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-\\.]+$",
                message = "El nombre solo puede contener letras, espacios, guiones o puntos."
        )
        String titulo,

        @Size(min = 3, max = 155, message = "La descripcion debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-]+$",
                message = "La descripcion solo puede contener letras, espacios y guiones."
        )
        String descripcion,

        @Size(min = 3, max = 155, message = "El lugar debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-]+$",
                message = "El lugar solo puede contener letras, espacios y guiones."
        )
        String lugar
) {
}
