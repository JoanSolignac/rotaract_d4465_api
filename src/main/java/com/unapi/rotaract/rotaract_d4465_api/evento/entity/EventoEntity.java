package com.unapi.rotaract.rotaract_d4465_api.evento.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Superclase base para todos los eventos del sistema Rotaract..
 *
 * Contiene los atributos comunes para cualquier tipo de evento:
 * título, descripción, lugar, requisitos, vigencia temporal y estado.
 *
 * Utiliza herencia JOINED para mantener organización y evitar duplicación
 * entre tipos específicos como Convocatoria o Proyecto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_evento")
@SuperBuilder
public abstract class EventoEntity {

    /**
     * Identificador único del evento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título del evento o convocatoria.
     */
    @Column(nullable = false, length = 100)
    private String titulo;

    /**
     * Descripción detallada del evento.
     */
    @Column(length = 500)
    private String descripcion;

    /**
     * Ubicación física o virtual del evento.
     */
    @Column(nullable = false)
    private String lugar;

    /**
     * Requisitos necesarios para participar.
     */
    @Column(nullable = false, length = 500)
    private String requisitos;

    /**
     * Fecha de inicio del evento.
     */
    @Column(nullable = false)
    private LocalDate fechaInicio;

    /**
     * Fecha de finalización del evento.
     */
    @Column(nullable = false)
    private LocalDate fechaFin;

    /**
     * Indica si el evento está activo.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
