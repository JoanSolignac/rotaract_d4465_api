package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import java.time.LocalDate;

/**
 * Representación de salida utilizada para exponer los datos completos de una convocatoria.
 * Este DTO concentra la información relevante para su visualización en interfaces públicas
 * o administrativas, incluyendo datos descriptivos, fechas clave, cupo disponible y metadatos
 * del club que la creó.
 *
 * Propósito:
 * - Estandarizar la estructura de respuesta enviada por la API.
 * - Evitar exponer directamente las entidades del dominio.
 * - Proveer una vista consolidada sin información sensible.
 */
public record ConvocatoriaResponseDto(
        Long id,
        String titulo,
        String descripcion,
        String requisitos,

        Integer cupoMaximo,
        Integer inscritos,
        LocalDate fechaPublicacion,
        LocalDate fechaCierre,

        LocalDate fechaInicioPostulacion,
        LocalDate fechaFinPostulacion,

        Long clubId,
        String clubNombre,

        String estado
) {}
