package com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.MisInscripcionesItemDto;
import org.springframework.data.domain.Page;

import java.util.List;

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

    // NUEVOS
    /**
     * Devuelve todas las inscripciones del usuario autenticado
     * (convocatorias y proyectos) en un formato unificado.
     */
    List<MisInscripcionesItemDto> obtenerMisInscripciones();

    /**
     * Cancela la inscripción del usuario autenticado a una convocatoria.
     */
    void cancelarInscripcionConvocatoria(Long convocatoriaId);

    /**
     * Cancela la inscripción del usuario autenticado a un proyecto.
     */
    void cancelarInscripcionProyecto(Long proyectoId);
}
