package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ConvocatoriaResponseDto(
        Long id,
        String requisitos,
        String tipo
) {}
