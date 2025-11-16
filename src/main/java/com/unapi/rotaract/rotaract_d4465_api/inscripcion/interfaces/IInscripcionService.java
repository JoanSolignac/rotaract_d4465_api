package com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import org.springframework.data.domain.Page;

public interface IInscripcionService {

    // Convocatorias
    void inscribirseEnConvocatoria(Long convocatoriaId);

    Page<InscripcionResponseDto> listarInscripcionesConvocatoria(Long convocatoriaId, int page, int size);

    // Proyectos
    void inscribirseEnProyecto(Long proyectoId);

    Page<InscripcionResponseDto> listarInscripcionesProyecto(Long proyectoId, int page, int size);

    // Gestión (aceptar / rechazar)
    void aceptarInscripcion(Long inscripcionId);

    void rechazarInscripcion(Long inscripcionId);
}
