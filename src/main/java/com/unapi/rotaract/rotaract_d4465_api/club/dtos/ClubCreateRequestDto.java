package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO de solicitud utilizado para la creación de un nuevo club Rotaract.
 *
 * Este objeto se emplea en las operaciones de creación (POST) para recibir
 * los datos necesarios al registrar un club dentro del sistema.
 *
 * @param nombre        Nombre público y único del club Rotaract.
 * @param departamento  Departamento o región donde se ubica el club.
 * @param ciudad        Ciudad o provincia donde opera el club.
 */
@Builder
public record ClubCreateRequestDto(

        @NotBlank(message = "El nombre del club es obligatorio.")
        @Size(min = 3, max = 155, message = "El nombre del club debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-\\.]+$",
                message = "El nombre solo puede contener letras, espacios, guiones o puntos."
        )
        String nombre,

        @NotBlank(message = "El departamento es obligatorio.")
        @Size(min = 3, max = 155, message = "El departamento debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-]+$",
                message = "El departamento solo puede contener letras, espacios y guiones."
        )
        String departamento,

        @NotBlank(message = "La ciudad es obligatoria.")
        @Size(min = 3, max = 155, message = "La ciudad debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-]+$",
                message = "La ciudad solo puede contener letras, espacios y guiones."
        )
        String ciudad
) { }
