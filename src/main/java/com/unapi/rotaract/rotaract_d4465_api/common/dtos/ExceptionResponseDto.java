package com.unapi.rotaract.rotaract_d4465_api.common.dtos;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

/**
 * Representa la estructura estándar de un error global manejado por el
 * controlador de excepciones de la aplicación.
 *
 * Este record se utiliza para serializar la información de error que se
 * devuelve al cliente cuando ocurre una excepción controlada. Contiene la
 * marca temporal, el código de estado HTTP, una descripción (o lista) de
 * errores y un mensaje legible por humanos.
 *
 * @param timestamp fecha en la que ocurrió el error (solo fecha)
 * @param status código de estado HTTP asociado al error
 * @param errors detalles técnicos o errores ocurridos
 * @param message mensaje legible destinado al cliente o consumidor del API
 */
@Builder
public record ExceptionResponseDto(
    LocalDate timestamp,
    int status,
    List<String> errors,
    String message
) {

}
