package com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la persistencia y consulta de inscripciones.
 */
public interface InscripcionRepository extends JpaRepository<InscripcionEntity, Long> {

    /**
     * Verifica si el usuario ya tiene una inscripción activa (PENDIENTE o ACEPTADA)
     * a cualquier convocatoria.
     */
    boolean existsByUsuarioIdAndConvocatoriaIsNotNullAndEstadoIn(
            Long usuarioId,
            List<InscripcionEntity.EstadoInscripcion> estados
    );

    /**
     * Verifica si el usuario ya está inscrito a un proyecto específico.
     */
    boolean existsByUsuarioIdAndProyectoId(Long usuarioId, Long proyectoId);

    /**
     * Devuelve las inscripciones asociadas a una convocatoria.
     */
    Page<InscripcionEntity> findByConvocatoriaId(Long convocatoriaId, Pageable pageable);

    /**
     * Devuelve las inscripciones asociadas a un proyecto.
     */
    Page<InscripcionEntity> findByProyectoId(Long proyectoId, Pageable pageable);

}
