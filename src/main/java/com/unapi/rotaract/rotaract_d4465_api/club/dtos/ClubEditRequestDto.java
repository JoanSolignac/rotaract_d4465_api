package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO utilizado para la actualización parcial de clubes Rotaract.
 *
 * Este objeto se emplea en operaciones de actualización (PATCH) para modificar
 * los datos existentes de un club. Solo los campos no nulos serán actualizados.
 *
 * @param nombre        Nombre público del club Rotaract.
 * @param departamento  Departamento o región donde se ubica el club.
 * @param ciudad        Ciudad o provincia donde opera el club.
 * @param descripcion   Descripción breve del club o sus actividades.
 */
@Builder
public record ClubEditRequestDto(

        @Size(min = 3, max = 155, message = "El nombre del club debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-\\.]+$",
                message = "El nombre solo puede contener letras, espacios, guiones o puntos."
        )
        String nombre,

        @Size(min = 3, max = 155, message = "El departamento debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-]+$",
                message = "El departamento solo puede contener letras, espacios y guiones."
        )
        String departamento,

        @Size(min = 3, max = 155, message = "La ciudad debe tener entre 3 y 155 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s\\-]+$",
                message = "La ciudad solo puede contener letras, espacios y guiones."
        )
        String ciudad,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String descripcion
) { }
