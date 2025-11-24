package com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la persistencia y consulta de inscripciones.
 *
 * Incluye métodos auxiliares para verificar inscripciones activas
 * y facilitar consultas por usuario, convocatoria y proyecto.
 */
public interface InscripcionRepository extends JpaRepository<InscripcionEntity, Long> {

    // ============================================================
    // VALIDACIONES DE INSCRIPCIÓN
    // ============================================================

    /**
     * Verifica si el usuario ya tiene una inscripción activa (PENDIENTE o ACEPTADA)
     * en alguna convocatoria.
     */
    boolean existsByUsuarioIdAndConvocatoriaIsNotNullAndEstadoIn(
            Long usuarioId,
            List<InscripcionEntity.EstadoInscripcion> estados
    );

    /**
     * Verifica si el usuario ya está inscrito a un proyecto específico.
     */
    boolean existsByUsuarioIdAndProyectoId(Long usuarioId, Long proyectoId);


    // ============================================================
    // CONSULTAS POR CONVOCATORIA / PROYECTO
    // ============================================================

    /**
     * Devuelve las inscripciones asociadas a una convocatoria.
     */
    Page<InscripcionEntity> findByConvocatoriaId(Long convocatoriaId, Pageable pageable);

    /**
     * Devuelve las inscripciones asociadas a un proyecto.
     */
    Page<InscripcionEntity> findByProyectoId(Long proyectoId, Pageable pageable);

    /**
     * Devuelve todas las inscripciones de un proyecto (sin paginar).
     * — Necesario para asistencia.
     */
    List<InscripcionEntity> findByProyectoId(Long proyectoId);


    // ============================================================
    // CONSULTAS POR PROYECTO + USUARIO (NUEVO — NECESARIO)
    // ============================================================

    /**
     * Devuelve la inscripción de un usuario en un proyecto específico.
     * — Necesario para asistencia.
     */
    List<InscripcionEntity> findByProyectoIdAndUsuarioId(Long proyectoId, Long usuarioId);


    // ============================================================
    // CONSULTAS POR USUARIO (EXISTENTES)
    // ============================================================

    /**
     * Devuelve todas las inscripciones realizadas por un usuario.
     */
    List<InscripcionEntity> findByUsuarioId(Long usuarioId);

    /**
     * Devuelve las inscripciones paginadas realizadas por un usuario.
     */
    Page<InscripcionEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

    /**
     * Devuelve todas las inscripciones del usuario que correspondan a convocatorias.
     */
    List<InscripcionEntity> findByUsuarioIdAndConvocatoriaIsNotNull(Long usuarioId);

    /**
     * Devuelve las inscripciones paginadas del usuario que correspondan a convocatorias.
     */
    Page<InscripcionEntity> findByUsuarioIdAndConvocatoriaIsNotNull(Long usuarioId, Pageable pageable);
}
