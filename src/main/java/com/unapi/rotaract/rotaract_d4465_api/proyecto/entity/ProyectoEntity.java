package com.unapi.rotaract.rotaract_d4465_api.proyecto.entity;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Entidad JPA que representa un proyecto dentro de la plataforma.
 *
 * Extiende la clase base {@link EventoEntity}, desde donde hereda atributos
 * comunes como título, descripción, requisitos y lugar. Define además un
 * conjunto de fechas específicas asociadas al ciclo de vida del proyecto,
 * incluyendo las fases de postulación y ejecución.
 *
 * Los campos de fechas no reutilizan los atributos fechaInicio y fechaFin
 * del evento base, sino que se declaran de manera explícita para mantener
 * claridad conceptual entre tipos de eventos distintos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("PROYECTO")
@SuperBuilder
public class ProyectoEntity extends EventoEntity {

    /**
     * Objetivo principal del proyecto, describe su propósito o finalidad.
     */
    @Column(nullable = false, length = 500)
    private String objetivo;

    /**
     * Fecha de inicio del periodo de postulación.
     */
    @Column(nullable = false)
    private LocalDate fechaInicioPostulacion;

    /**
     * Fecha de término del periodo de postulación.
     */
    @Column(nullable = false)
    private LocalDate fechaFinPostulacion;

    /**
     * Fecha de inicio de la fase de ejecución del proyecto.
     */
    @Column(nullable = false)
    private LocalDate fechaInicioProyecto;

    /**
     * Fecha de finalización de la fase de ejecución del proyecto.
     */
    @Column(nullable = false)
    private LocalDate fechaFinProyecto;

    /**
     * Estado actual del proyecto dentro de su ciclo de vida.
     * Permite diferenciar entre fase propuesta, en postulación,
     * en ejecución, finalización o cancelación.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProyecto estadoProyecto;

    /**
     * Enumeración que define las etapas del proyecto.
     * Estas fases son gestionadas parcialmente mediante el scheduler.
     */
    public enum EstadoProyecto {
        PROPUESTO,
        EN_POSTULACION,
        EN_EJECUCION,
        FINALIZADO,
        CANCELADO
    }
}
