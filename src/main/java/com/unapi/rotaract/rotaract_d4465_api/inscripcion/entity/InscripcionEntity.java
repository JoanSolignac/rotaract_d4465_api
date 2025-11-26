package com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una inscripción de un usuario a una convocatoria o a un proyecto.
 * Sólo uno de los campos convocatoria o proyecto debe estar informado en cada registro.
 */
@Entity
@Table(name = "inscripciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realiza la inscripción.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    /**
     * Convocatoria asociada a la inscripción, si aplica.
     * Es null cuando la inscripción pertenece a un proyecto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convocatoria_id")
    private ConvocatoriaEntity convocatoria;

    /**
     * Proyecto asociado a la inscripción, si aplica.
     * Es null cuando la inscripción pertenece a una convocatoria.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private ProyectoEntity proyecto;

    /**
     * Fecha y hora en la que se registró la inscripción.
     */
    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Estado actual de la inscripción.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInscripcion estado;

    /**
     * Estados posibles de una inscripción.
     */
    public enum EstadoInscripcion {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA,
        CANCELADA
    }
}
