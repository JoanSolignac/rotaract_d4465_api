package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para crear club asignando presidente")
public record ClubCreateWithPresidenteRequestDto(

        @Schema(description = "Nombre del club", example = "Rotaract Iquitos")
        @NotBlank
        String nombre,

        @Schema(description = "Departamento del club", example = "Loreto")
        @NotBlank
        String departamento,

        @Schema(description = "Ciudad del club", example = "Iquitos")
        @NotBlank
        String ciudad,

        @Schema(description = "ID del usuario que será presidente del club")
        @NotNull
        Long presidenteId

) {}
