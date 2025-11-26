package com.unapi.rotaract.rotaract_d4465_api.miembro.dtos;

public record MiembroResponseDto(
        Long id,
        String nombre,
        String correo,
        String rol,
        Boolean activo
) {}
