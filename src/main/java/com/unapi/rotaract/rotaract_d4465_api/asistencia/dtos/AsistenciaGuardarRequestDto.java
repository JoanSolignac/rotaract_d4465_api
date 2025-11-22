package com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para registrar asistencia.
 * Contiene únicamente los IDs de usuarios marcados como PRESENTE.
 * Los demás usuarios inscritos serán registrados como FALTA.
 */
public record AsistenciaGuardarRequestDto(

        @NotNull(message = "Debe enviarse la lista de presentes.")
        List<Long> usuariosPresentesIds

) {}
