package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO de solicitud utilizado para la creación de una nueva convocatoria Rotaract.
 *
 * Este objeto se emplea en las operaciones de creación (POST) para recibir
 * los datos necesarios al registrar una convocatoria dentro del sistema.
 *
 * @param titulo        Título o nombre público de la convocatoria.
 * @param descripcion   Descripción breve u observaciones sobre la convocatoria.
 * @param fechaInicio   Fecha de inicio de la convocatoria.
 * @param fechaFin      Fecha de cierre de la convocatoria.
 * @param lugar         Lugar físico o virtual donde se desarrollará la convocatoria.
 * @param requisitos    Condiciones o requisitos que deben cumplir los interesados.
 */
@Builder
public record ConvocatoriaCreateRequestDto(

        @NotBlank(message = "El título de la convocatoria es obligatorio.")
        @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s\\-\\.]+$",
                message = "El título solo puede contener letras, números, espacios, guiones o puntos."
        )
        String titulo,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String descripcion,

        @NotNull(message = "La fecha de inicio es obligatoria.")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria.")
        LocalDate fechaFin,

        @NotBlank(message = "El lugar es obligatorio.")
        @Size(min = 3, max = 255, message = "El lugar debe tener entre 3 y 255 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s\\-\\.\\,\\-]+$",
                message = "El lugar solo puede contener letras, números, espacios, comas, guiones o puntos."
        )
        String lugar,

        @NotBlank(message = "Los requisitos son obligatorios.")
        @Size(min = 5, max = 500, message = "Los requisitos deben tener entre 5 y 500 caracteres.")
        String requisitos
) { }
