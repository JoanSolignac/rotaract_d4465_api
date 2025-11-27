package com.unapi.rotaract.rotaract_d4465_api.auth.dtos;

import java.time.LocalDate;

public record UsuarioPerfilResponseDto(
        Long id,
        String nombre,
        String correo,
        String ciudad,
        LocalDate fechaNacimiento,
        String rol,
        Long clubId
) {}
