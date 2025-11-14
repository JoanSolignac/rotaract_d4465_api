package com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad JPA que representa una Convocatoria emitida por un club Rotaract.
 *
 * Una convocatoria agrupa información sobre un evento, proyecto o llamada a participación
 * dentro del distrito. Incluye datos de identificación, vigencia temporal, requisitos y
 * estado de actividad. Está siempre asociada a un {@link ClubEntity} emisor.
 */
@Entity
@Table(name = "convocatorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvocatoriaEntity {
    /**
     * Identificador único autogenerado de la convocatoria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título público de la convocatoria. Describe brevemente el propósito.
     */
    @Column(nullable = false, length = 100)
    private String titulo;

    /**
     * Descripción extendida u observaciones adicionales sobre la convocatoria.
     * Puede ser nula si no se requiere información adicional.
     */
    @Column(nullable = true, length = 500)
    private String descripcion;

    /**
     * Fecha en la que inicia la vigencia de la convocatoria. A partir de esta fecha se considera activa.
     */
    @Column(nullable = false)
    private LocalDate fechaInicio;

    /**
     * Fecha de cierre o finalización de la convocatoria. Después de esta fecha deja de aceptarse participación.
     */
    @Column(nullable = false)
    private LocalDate fechaFin;

    /**
     * Lugar físico o virtual donde se desarrollará el evento asociado a la convocatoria.
     */
    @Column(nullable = false)
    private String lugar;

    /**
     * Requisitos que deben cumplir los interesados para participar (texto descriptivo consolidado).
     */
    @Column(nullable = false, length = 500)
    private String requisitos;

    /**
     * Indica si la convocatoria está activa y visible. Si es false, se considera cerrada o archivada.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Club emisor de la convocatoria. Relación muchos-a-uno con {@link ClubEntity}.
     *
     * - fetch LAZY: evita cargar datos del club hasta que sean necesarios.
     * - optional = false: una convocatoria siempre pertenece a un club.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private ClubEntity club;
}
