package com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos;

import java.time.LocalDate;

/**
 * DTO de respuesta utilizado para exponer información pública de un proyecto.
 * Proporciona una estructura segura y estable para la capa de presentación,
 * evitando exponer directamente la entidad JPA y presentando únicamente los
 * valores relevantes y autorizados para el consumo del cliente.
 *
 * Incluye tanto información descriptiva como las fechas de postulación y
 * ejecución, así como datos básicos del club responsable y el estado actual
 * del proyecto.
 */
public record ProyectoResponseDto(

        /**
         * Identificador único del proyecto.
         */
        Long id,

        /**
         * Título asignado al proyecto.
         */
        String titulo,

        /**
         * Descripción general que detalla el propósito y características del proyecto.
         */
        String descripcion,

        /**
         * Objetivo principal del proyecto.
         */
        String objetivo,

        /**
         * Condiciones o requisitos necesarios para participar en el proyecto.
         */
        String requisitos,

        /**
         * Lugar donde se llevará a cabo el proyecto.
         */
        String lugar,

        /**
         * Fecha en la que inicia la fase de postulación.
         */
        LocalDate fechaInicioPostulacion,

        /**
         * Fecha en la que finaliza la fase de postulación.
         */
        LocalDate fechaFinPostulacion,

        /**
         * Fecha de inicio de la ejecución del proyecto.
         */
        LocalDate fechaInicioProyecto,

        /**
         * Fecha de término de la ejecución del proyecto.
         */
        LocalDate fechaFinProyecto,

        /**
         * Identificador del club responsable del proyecto.
         */
        Long clubId,

        /**
         * Nombre del club responsable del proyecto.
         */
        String clubNombre,

        /**
         * Estado actual del proyecto, representado como cadena.
         */
        String estadoProyecto

) {}
