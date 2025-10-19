package com.unapi.rotaract.rotaract_d4465_api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link RolEntity}.
 *
 * Permite operaciones CRUD y consultas auxiliares relacionadas con roles del sistema.
 */
@Repository
public interface RolRepository extends JpaRepository<RolEntity, Long> {
    /**
     * Busca un rol por su nombre/código.
     *
     * @param rolCode nombre o código del rol a buscar
     * @return Optional que contiene el {@link RolEntity} si existe
     */
    Optional<RolEntity> findByNombre(String rolCode);
}
