package com.unapi.rotaract.rotaract_d4465_api.asistencia.repository;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de asistencias.
 * Permite consultar registros por proyecto, por usuario y por combinación.
 */
public interface AsistenciaRepository extends JpaRepository<AsistenciaEntity, Long> {

    /**
     * Retorna la asistencia de un usuario en un proyecto específico.
     * Debe existir máximo un registro por usuario y proyecto.
     */
    Optional<AsistenciaEntity> findByProyectoIdAndUsuarioId(Long proyectoId, Long usuarioId);

    /**
     * Lista todas las asistencias asociadas a un proyecto.
     */
    List<AsistenciaEntity> findByProyectoId(Long proyectoId);

    /**
     * Lista todas las asistencias de un usuario.
     * (Útil para historial si lo necesitas luego).
     */
    List<AsistenciaEntity> findByUsuarioId(Long usuarioId);

}
