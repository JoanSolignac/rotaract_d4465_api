package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Representa los datos requeridos para la creación de una nueva convocatoria.
 * Este DTO es utilizado por el controlador durante el proceso de registro.
 *
 * El club ya no se recibe como parámetro, debido a que se obtiene
 * automáticamente desde la sesión del usuario autenticado.
 *
 * Validaciones aplicadas:
 * - Los campos obligatorios deben contener valores válidos.
 * - Las fechas deben ser coherentes cronológicamente durante la validación
 *   realizada en la capa de servicio.
 */
public record ConvocatoriaCreateRequestDto(

        /**
         * Título de la convocatoria.
         * No debe estar vacío y tiene un límite máximo de 120 caracteres.
         */
        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 120, message = "El título no debe exceder 120 caracteres.")
        String titulo,

        /**
         * Descripción de la convocatoria.
         * Es opcional, pero su extensión no debe superar los 500 caracteres.
         */
        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres.")
        String descripcion,

        /**
         * Cupo máximo permitido para la convocatoria.
         * Debe ser un número positivo.
         */
        @NotNull(message = "El cupo máximo es obligatorio.")
        @Positive(message = "El cupo máximo debe ser un número positivo.")
        Integer cupoMaximo,

        /**
         * Fecha desde la cual será visible públicamente la convocatoria.
         */
        @NotNull(message = "La fecha de publicación es obligatoria.")
        LocalDate fechaPublicacion,

        /**
         * Fecha límite en la cual la convocatoria dejará de estar visible.
         */
        @NotNull(message = "La fecha de cierre es obligatoria.")
        LocalDate fechaCierre,

        /**
         * Fecha de inicio del proceso de postulación.
         */
        @NotNull(message = "La fecha de inicio de postulación es obligatoria.")
        LocalDate fechaInicioPostulacion,

        /**
         * Fecha límite del proceso de postulación.
         */
        @NotNull(message = "La fecha de fin de postulación es obligatoria.")
        LocalDate fechaFinPostulacion,

        /**
         * Requisitos opcionales para la postulación.
         * Su extensión no debe exceder los 500 caracteres.
         */
        @Size(max = 500, message = "Los requisitos no deben exceder 500 caracteres.")
        String requisitos

) {}
