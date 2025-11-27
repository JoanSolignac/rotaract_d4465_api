package com.unapi.rotaract.rotaract_d4465_api.asistencia.repository;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<AsistenciaEntity, Long> {

    /**
     * Retorna la asistencia de un usuario en un proyecto específico.
     */
    Optional<AsistenciaEntity> findByProyectoIdAndUsuarioId(Long proyectoId, Long usuarioId);

    /**
     * Lista todas las asistencias asociadas a un proyecto.
     */
    List<AsistenciaEntity> findByProyectoId(Long proyectoId);

    /**
     * Lista todas las asistencias de un usuario.
     */
    List<AsistenciaEntity> findByUsuarioId(Long usuarioId);

}
