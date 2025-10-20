package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import lombok.Builder;
import java.time.LocalDate;

/**
 * DTO de respuesta que representa la información pública de un club Rotaract.
 *
 * Este objeto se utiliza en las operaciones de lectura (GET) para enviar los datos
 * de los clubes a la capa de presentación o al frontend.
 * No incluye información sensible ni lógica de negocio.
 *
 * @param id              Identificador único del club.
 * @param nombre          Nombre oficial del club Rotaract.
 * @param departamento    Departamento o región donde opera el club.
 * @param ciudad          Ciudad o provincia correspondiente al club.
 * @param fechaCreacion   Fecha de creación o registro del club.
 * @param activo          Estado actual del club (activo o inactivo).
 */
@Builder
public record ClubResponseDto(
        Long id,
        String nombre,
        String departamento,
        String ciudad,
        LocalDate fechaCreacion,
        boolean activo
) { }
