package com.unapi.rotaract.rotaract_d4465_api.auth.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de respuesta para los procesos de autenticación del sistema Rotaract D4465.
 *
 * Se utiliza tanto para el inicio de sesión como para el registro exitoso.
 * Contiene los tokens generados y la información básica del usuario autenticado.
 */
public record AuthResponseDto(

    @NotBlank(message = "El token de acceso no puede estar vacío")
    String accessToken,

    @NotBlank(message = "El token de renovación no puede estar vacío")
    String refreshToken,

    @NotBlank(message = "El correo del usuario no puede estar vacío")
    String correo,

    @NotBlank(message = "El rol del usuario no puede estar vacío")
    String rol

) {}
