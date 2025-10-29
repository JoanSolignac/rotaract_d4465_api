package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder

public record ConvocatoriaEditRequestDto(
        @Size(min = 5, max = 1000, message = "Entre 5 y 1000 caracteres.")
        String requisitos,

        @Size(min = 3, max = 100, message = "Entre 3 y 100 caracteres.")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-_/]+$")
        String tipo
) {
}
