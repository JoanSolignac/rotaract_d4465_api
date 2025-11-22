package com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA encargado de gestionar la persistencia de entidades
 * {@link ConvocatoriaEntity}. Proporciona acceso a las operaciones CRUD
 * básicas mediante {@link JpaRepository} y expone consultas derivadas
 * específicas para este módulo.
 *
 * Comportamiento general:
 * - Los métodos retornan listas vacías cuando no existen coincidencias.
 * - Las consultas derivadas siguen convención de nombres estándar de Spring Data JPA.
 */
@Repository
public interface ConvocatoriaRepository extends JpaRepository<ConvocatoriaEntity, Long> {

    /**
     * Recupera todas las convocatorias asociadas a un club específico.
     *
     * @param clubId identificador del club creador
     * @return lista de convocatorias pertenecientes al club indicado
     */
    List<ConvocatoriaEntity> findByClubId(Long clubId);

    /**
     * Recupera una página de convocatorias asociadas a un club específico.
     *
     * @param clubId   identificador del club creador
     * @param pageable información de paginación
     * @return página de convocatorias pertenecientes al club indicado
     */
    Page<ConvocatoriaEntity> findByClubId(Long clubId, Pageable pageable);

    /**
     * Obtiene todas las convocatorias que presenten un estado determinado.
     *
     * @param estado estado de la convocatoria según {@link EventoEntity.EstadoEvento}
     * @return lista de convocatorias filtradas por estado
     */
    List<ConvocatoriaEntity> findByEstado(EventoEntity.EstadoEvento estado);
}
