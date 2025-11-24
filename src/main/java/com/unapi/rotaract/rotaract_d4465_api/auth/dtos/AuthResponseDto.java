package com.unapi.rotaract.rotaract_d4465_api.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record AuthResponseDto(

        @NotBlank(message = "El token de acceso no puede estar vacío")
        String accessToken,

        @NotBlank(message = "El token de renovación no puede estar vacío")
        String refreshToken,

        @NotBlank(message = "El correo del usuario no puede estar vacío")
        String correo,

        @NotBlank(message = "El rol del usuario no puede estar vacío")
        String rol,

        String nombre,

        Long id
) {}
