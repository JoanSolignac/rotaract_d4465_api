package com.unapi.rotaract.rotaract_d4465_api.club.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;

@Repository
public interface ClubRepository extends JpaRepository<ClubEntity, Long> {

    Optional<ClubEntity> findByNombre(String nombreClub);
	/**
	 * Repositorio JPA para la entidad {@link ClubEntity}.
	 *
	 * Proporciona operaciones CRUD y paginación/herencias estándar a
	 * través de la interfaz {@link JpaRepository}. Se recomienda usar este
	 * repositorio para acceder y manipular los registros de la tabla
	 * "clubs" desde la capa de servicio.
	 *
	 * Ejemplos de uso (en un servicio):
	 *   List<ClubEntity> clubs = clubRepository.findAll();
	 *   Optional<ClubEntity> club = clubRepository.findById(id);
	 */
}
