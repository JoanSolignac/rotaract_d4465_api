package com.unapi.rotaract.rotaract_d4465_api.miembro.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.MiembroResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.TotalUsuariosResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.RepresentanteResponseDto;

public interface IMiembroService {

    /**
     * Lista paginada de todos los usuarios (miembros).
     */
    Page<MiembroResponseDto> listarMiembrosPaginados(Pageable pageable);

    /**
     * Obtiene el total de usuarios registrados en la plataforma.
     */
    TotalUsuariosResponseDto obtenerTotalUsuarios();

    /**
     * Obtiene el representante distrital del sistema.
     */
    RepresentanteResponseDto obtenerRepresentanteDistrital();
}
