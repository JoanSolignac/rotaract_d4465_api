package com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos;

public record InscripcionResponseDto(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        Long convocatoriaId,
        String convocatoriaTitulo,
        String estado,
        String tipoInscripcion
) {
}
