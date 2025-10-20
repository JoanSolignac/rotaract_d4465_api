package com.unapi.rotaract.rotaract_d4465_api.club.repository;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link ClubEntity}.
 *
 * Provee operaciones CRUD y de paginación/herencia a través de {@link JpaRepository}.
 * Aquí se pueden añadir consultas personalizadas relacionadas con clubes cuando
 * la lógica de negocio lo requiera (por ejemplo, búsqueda por ciudad o estado).
 */
@Repository
public interface ClubRepository extends JpaRepository<ClubEntity, Long> {

    /**
     * Busca un club por su nombre exacto.
     *
     * @param nombre nombre del club a buscar
     * @return {@link Optional} que contiene el {@link ClubEntity} si existe, o vacío en caso contrario
     */
    Optional<ClubEntity> findByNombre(String nombre);


    /**
     * Retorna una página de clubes.
     *
     * @param pageable objeto de paginación
     * @return {@link Page} que contiene el {@link ClubEntity} paginados
     */
    Page<ClubEntity> findAllBy(Pageable pageable);
}
