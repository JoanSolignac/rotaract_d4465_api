package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Estructura utilizada para actualizar parcialmente una convocatoria existente.
 * Contiene únicamente campos opcionales, permitiendo que el cliente envíe solo
 * los valores que desea modificar. La capa de servicio se encarga de aplicar
 * las actualizaciones válidas y mantener la coherencia temporal entre las fechas.
 *
 * Consideraciones:
 * - Los campos null no producen cambios en la entidad.
 * - Se validan tamaños máximos y valores positivos cuando corresponde.
 * - El proceso de edición no permite modificar el club asociado ni la fecha de publicación.
 */
public record ConvocatoriaEditRequestDto(

        @Size(max = 120, message = "El título no debe exceder 120 caracteres.")
        String titulo,

        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres.")
        String descripcion,

        @Positive(message = "El cupo máximo debe ser un número positivo.")
        Integer cupoMaximo,

        LocalDate fechaCierre,

        LocalDate fechaInicioPostulacion,

        LocalDate fechaFinPostulacion,

        @Size(max = 500, message = "Los requisitos no deben exceder 500 caracteres.")
        String requisitos

) {}
