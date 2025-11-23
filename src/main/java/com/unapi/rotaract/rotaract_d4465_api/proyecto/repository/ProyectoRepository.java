package com.unapi.rotaract.rotaract_d4465_api.proyecto.repository;

import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link ProyectoEntity}.
 *
 * Proporciona operaciones CRUD estándar (heredadas de {@link JpaRepository}) y métodos
 * de consulta derivados por convención de nombres de Spring Data JPA para filtrar proyectos
 * por club, estado, coincidencias en título y fechas clave (inicio y fin).
 *
 * Características generales:
 * - Devuelve listas o páginas vacías cuando no existen coincidencias.
 * - No implementa lógica de negocio; sólo delega en el motor de Spring Data JPA.
 * - Las excepciones de acceso a datos (DataAccessException y subclases) se propagan a capas superiores.
 */
public interface ProyectoRepository extends JpaRepository<ProyectoEntity, Long> {

    /**
     * Obtiene todos los proyectos pertenecientes a un club.
     *
     * @param clubId identificador del club dueño del proyecto
     * @return lista de proyectos del club (posiblemente vacía si no hay proyectos)
     */
    List<ProyectoEntity> findByClubId(Long clubId);

    /**
     * Recupera proyectos filtrados por su estado actual.
     *
     * @param estado estado del proyecto (enum definido en la entidad)
     * @return lista de proyectos en el estado indicado; puede ser vacía
     */
    List<ProyectoEntity> findByEstadoProyecto(ProyectoEntity.EstadoProyecto estado);

    /**
     * Busca proyectos cuyo título contenga (ignorando mayúsculas/minúsculas) el texto especificado.
     * Permite paginar y ordenar los resultados.
     *
     * @param titulo fragmento de texto a buscar dentro del título
     * @param pageable información de paginación y orden
     * @return página de proyectos que cumplen el criterio; puede venir vacía
     */
    Page<ProyectoEntity> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    /**
     * Recupera proyectos cuya fecha de inicio coincide exactamente con la proporcionada.
     *
     * @param fechaInicioProyecto fecha de inicio del proyecto
     * @return lista de proyectos que comienzan en esa fecha; puede ser vacía
     */
    List<ProyectoEntity> findByFechaInicioProyecto(LocalDate fechaInicioProyecto);

    /**
     * Lista los proyectos de un club ordenados por fecha de inicio en orden descendente
     * (más recientes primero).
     *
     * @param clubId identificador del club
     * @return lista ordenada de proyectos; puede ser vacía
     */
    List<ProyectoEntity> findByClubIdOrderByFechaInicioProyectoDesc(Long clubId);

    /**
     * Recupera proyectos cuya fecha de fin coincide exactamente con la proporcionada.
     *
     * @param fechaFinProyecto fecha de finalización del proyecto
     * @return lista de proyectos que finalizan ese día; puede ser vacía
     */
    List<ProyectoEntity> findByFechaFinProyecto(LocalDate fechaFinProyecto);

    /**
     * Cuenta la cantidad total de proyectos asociados a un club.
     * Útil para métricas y cálculos de paginación.
     *
     * @param clubId identificador del club
     * @return número de proyectos del club
     */
    long countByClubId(Long clubId);
}
