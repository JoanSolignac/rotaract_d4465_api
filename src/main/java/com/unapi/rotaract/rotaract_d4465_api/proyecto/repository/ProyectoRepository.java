package com.unapi.rotaract.rotaract_d4465_api.proyecto.repository;

import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * Utiliza consultas derivadas de nombres de método para generar
     * automáticamente la sentencia correspondiente.
     *
     * @param clubId identificador del club responsable del proyecto
     * @return lista de proyectos pertenecientes al club indicado
     */
    List<ProyectoEntity> findByClubId(Long clubId);

    /**
     * Obtiene los proyectos cuyo estado coincide con el parámetro recibido.
     * Útil para filtrar proyectos por fase del ciclo de vida.
     *
     * @param estado estado del proyecto a consultar
     * @return lista de proyectos que se encuentran en el estado especificado
     */
    List<ProyectoEntity> findByEstadoProyecto(ProyectoEntity.EstadoProyecto estado);

    Page<ProyectoEntity> findByTituloContainingIgnoreCase(String titulo, org.springframework.data.domain.Pageable pageable);

}
