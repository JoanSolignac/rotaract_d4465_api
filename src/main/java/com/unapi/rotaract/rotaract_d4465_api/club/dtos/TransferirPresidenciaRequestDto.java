package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import jakarta.validation.constraints.NotNull;

public record TransferirPresidenciaRequestDto(
        @NotNull Long nuevoPresidenteId
) {}
