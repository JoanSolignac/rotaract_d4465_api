package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO utilizado para la actualización parcial de convocatorias Rotaract.
 *
 * Este objeto se emplea en operaciones de actualización (PATCH) para modificar
 * los datos existentes de una convocatoria. Solo los campos no nulos serán actualizados.
 *
 * @param titulo       Título o nombre público de la convocatoria.
 * @param descripcion  Descripción breve u observaciones sobre la convocatoria.
 * @param fechaInicio  Fecha de inicio de la convocatoria.
 * @param fechaFin     Fecha de cierre de la convocatoria.
 * @param lugar        Lugar físico o virtual donde se desarrollará la convocatoria.
 * @param requisitos   Condiciones o requisitos que deben cumplir los interesados.
 */
@Builder
public record ConvocatoriaEditRequestDto(

        @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s\\-\\.]+$",
                message = "El título solo puede contener letras, números, espacios, guiones o puntos."
        )
        String titulo,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String descripcion,

        LocalDate fechaInicio,

        LocalDate fechaFin,

        @Size(min = 3, max = 255, message = "El lugar debe tener entre 3 y 255 caracteres.")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s\\-\\.\\,\\-]+$",
                message = "El lugar solo puede contener letras, números, espacios, comas, guiones o puntos."
        )
        String lugar,

        @Size(min = 5, max = 500, message = "Los requisitos deben tener entre 5 y 500 caracteres.")
        String requisitos,

        Boolean activo
) { }
