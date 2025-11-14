package com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos;

import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO de respuesta que representa la información pública y consolidada de una convocatoria Rotaract.
 *
 * Este objeto se devuelve en las operaciones de lectura (GET) y después de crear/actualizar
 * una convocatoria para mostrar el estado actual completo. A diferencia de
 * {@link com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto} (usado para
 * actualizaciones parciales), este DTO no se valida para entrada sino que expone los datos ya
 * persistidos y normalizados.
 *
 * No incluye lógica de negocio ni campos sensibles; su propósito es transportar información
 * hacia la capa de presentación.
 *
 * @param id           Identificador único de la convocatoria.
 * @param nombreClub   Nombre oficial del club que emite la convocatoria.
 * @param titulo       Título público de la convocatoria.
 * @param descripcion  Descripción breve u observaciones generales de la convocatoria.
 * @param fechaInicio  Fecha desde la cual la convocatoria se considera abierta / vigente.
 * @param fechaFin     Fecha límite o de cierre para postulación/participación.
 * @param lugar        Ubicación física o virtual donde se desarrollará / aplica la convocatoria.
 * @param requisitos   Condiciones o requisitos que deben cumplir los interesados (texto consolidado).
 * @param activo       Estado actual de la convocatoria (true = activa / visible, false = inactiva / cerrada).
 */
@Builder
public record ConvocatoriaResponseDto(
        Long id,
        String nombreClub,
        String titulo,
        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String lugar,
        String requisitos,
        Boolean activo
) {}
