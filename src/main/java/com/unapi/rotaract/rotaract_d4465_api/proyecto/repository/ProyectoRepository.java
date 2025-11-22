package com.unapi.rotaract.rotaract_d4465_api.proyecto.repository;

import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA encargado de la persistencia y consulta de proyectos.
 * Extiende {@link JpaRepository} para proporcionar operaciones CRUD completas
 * y define métodos auxiliares basados en la convención de Spring Data JPA
 * que permiten consultas específicas sin requerir implementación manual.
 */
public interface ProyectoRepository extends JpaRepository<ProyectoEntity, Long> {

    /**
     * Recupera todos los proyectos asociados a un club específico.
     *
     * @param clubId identificador del club responsable del proyecto
     * @return lista de proyectos pertenecientes al club indicado
     */
    List<ProyectoEntity> findByClubId(Long clubId);

    /**
     * Obtiene los proyectos cuyo estado coincide con el parámetro recibido.
     *
     * @param estado estado del proyecto a consultar
     * @return lista de proyectos que se encuentran en el estado especificado
     */
    List<ProyectoEntity> findByEstadoProyecto(ProyectoEntity.EstadoProyecto estado);

    /**
     * Busca proyectos cuyo título contenga el texto indicado (ignorando mayúsculas/minúsculas).
     */
    Page<ProyectoEntity> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    /**
     * Proyectos cuya fecha de inicio de ejecución coincide con la fecha dada.
     * Útil para activar asistencia cuando comienza el proyecto.
     */
    List<ProyectoEntity> findByFechaInicioProyecto(LocalDate fechaInicioProyecto);

    /**
     * Proyectos cuya fecha de fin de ejecución coincide con la fecha dada.
     * Útil para cerrar asistencia y marcar faltas cuando el proyecto termina.
     */
    List<ProyectoEntity> findByFechaFinProyecto(LocalDate fechaFinProyecto);
}
