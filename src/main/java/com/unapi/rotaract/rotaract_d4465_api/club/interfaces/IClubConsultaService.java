package com.unapi.rotaract.rotaract_d4465_api.club.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubDetalleResponseDto;

/**
 * Servicio de consulta para obtener información detallada de un club,
 * incluyendo presidente, integrantes paginados, proyectos, convocatorias
 * y métricas asociadas.
 */
public interface IClubConsultaService {

    /**
     * Obtiene el detalle completo de un club específico.
     *
     * @param clubId ID del club a consultar
     * @param page   número de página para la paginación de integrantes
     * @param size   tamaño de página para la paginación de integrantes
     * @return DTO con información detallada del club
     */
    ClubDetalleResponseDto obtenerDetalleClub(Long clubId, int page, int size);

}
