package com.unapi.rotaract.rotaract_d4465_api.auth.repository;

import java.util.Optional;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;

/**
 * Repositorio JPA para la entidad {@link UsuarioEntity}.
 *
 * Provee operaciones CRUD básicas (heredadas de {@link JpaRepository}) y
 * consultas customizadas relacionadas con usuarios, por ejemplo buscar por correo.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param correo correo electrónico del usuario a buscar
     * @return Optional que contiene el {@link UsuarioEntity} si existe
     */
    Optional<UsuarioEntity> findByCorreo(String correo);

    @Query("SELECT r FROM RolEntity r WHERE r.nombre = :nombre")
    Optional<RolEntity> findRolByNombre(String nombre);

}
