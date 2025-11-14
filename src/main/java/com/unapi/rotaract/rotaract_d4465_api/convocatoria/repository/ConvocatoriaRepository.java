package com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link ConvocatoriaEntity}.
 *
 * Extiende {@link JpaRepository} para proporcionar operaciones CRUD, paginación y
 * funcionalidades de persistencia estándar. Se pueden definir aquí consultas
 * especializadas relacionadas con la gestión y ciclo de vida de las convocatorias.
 */
@Repository
public interface ConvocatoriaRepository extends JpaRepository<ConvocatoriaEntity, Long> {

    /**
     * Busca una convocatoria por su identificador único.
     *
     * @param id id de la convocatoria a buscar
     * @return {@link Optional} que contiene el {@link ConvocatoriaEntity} si existe, o vacío en caso contrario
     */
    Optional<ConvocatoriaEntity> findById(long id);

    /**
     * Recupera las convocatorias activas cuya fecha de fin ya pasó antes de la fecha especificada.
     * Útil para procesos de cierre automático o archivado de convocatorias vencidas.
     *
     * @param date fecha de referencia para comparar el campo fechaFin
     * @return lista de convocatorias activas vencidas; puede estar vacía si ninguna coincide
     */
    List<ConvocatoriaEntity> findByActivoTrueAndFechaFinBefore(LocalDate date);

    /**
     * Desactiva en una única operación todas las convocatorias activas cuya fechaFin es anterior a {@code today}.
     * Utiliza un UPDATE masivo para evitar cargar y persistir cada entidad individualmente, mejorando el rendimiento
     * en grandes volúmenes.
     *
     * @param today fecha de referencia (normalmente la fecha actual)
     * @return número de filas afectadas (convocatorias desactivadas)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE ConvocatoriaEntity c SET c.activo = false WHERE c.activo = true AND c.fechaFin < :today")
    int deactivateExpired(@Param("today") LocalDate today);
}
