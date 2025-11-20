package com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO utilizado para la actualización parcial de un proyecto.
 * Permite modificar únicamente los campos enviados, manteniendo sin cambios
 * aquellos valores que se envíen como null. Es adecuado para operaciones tipo PATCH.
 *
 * Las validaciones aplicadas aseguran coherencia mínima en los datos,
 * especialmente en campos textuales.
 */
public record ProyectoEditRequestDto(

        /**
         * Descripción general del proyecto.
         * Es opcional, pero si se envía debe cumplir con la longitud permitida.
         */
        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres.")
        String descripcion,

        /**
         * Objetivo del proyecto. Puede actualizarse de forma independiente.
         */
        String objetivo,

        Integer cupoMaximo,

        /**
         * Fecha de inicio de la fase de postulación.
         * Si se envía, será validada en la capa de servicio.
         */
        LocalDate fechaInicioPostulacion,

        /**
         * Fecha de fin de la fase de postulación.
         */
        LocalDate fechaFinPostulacion,

        /**
         * Fecha real de inicio de ejecución del proyecto.
         */
        LocalDate fechaInicioProyecto,

        /**
         * Fecha real de finalización del proyecto.
         */
        LocalDate fechaFinProyecto,

        /**
         * Condiciones necesarias para participar del proyecto.
         * Debe respetar la longitud máxima cuando se incluye.
         */
        @Size(max = 500, message = "Los requisitos no deben exceder 500 caracteres.")
        String requisitos,

        /**
         * Lugar donde se ejecutará el proyecto.
         */
        String lugar,

        String titulo

) {}
