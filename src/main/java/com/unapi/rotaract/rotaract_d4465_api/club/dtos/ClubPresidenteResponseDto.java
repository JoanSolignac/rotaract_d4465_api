package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO de respuesta para la información de un club dentro del sistema Rotaract D4465.
 * Este objeto se utiliza en las respuestas de la API al consultar, listar o registrar clubes.
 * Proporciona los datos públicos esenciales del club, como su nombre, ubicación
 * y fecha de creación, sin exponer información sensible o interna.
 */
@Builder
public record ClubPresidenteResponseDto(


    @NotNull(message = "El identificador del club no puede ser nulo")
    Long id,

    @NotBlank(message = "El nombre del club no puede estar vacío")
    String nombre,

    @NotBlank(message = "El departamento no puede estar vacío")
    String departamento,

    @NotBlank(message = "La ciudad no puede estar vacía")
    String ciudad,
    
    @NotNull(message = "La fecha de creación no puede ser nula")
    LocalDate fechaCreacion

) {}
