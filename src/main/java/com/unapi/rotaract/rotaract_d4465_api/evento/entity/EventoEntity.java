package com.unapi.rotaract.rotaract_d4465_api.evento.entity;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Clase base para eventos dentro de la plataforma,
 * utilizada por Proyectos y Convocatorias.
 *
 * Maneja únicamente:
 * - publicación
 * - cierre
 * - cupos
 * - estado general del evento
 *
 * Las fechas de postulación y ejecución existen únicamente
 * en las entidades hijas.
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    /**
     * Lugar donde se desarrollará el evento.
     */
    private String lugar;

    /**
     * Requisitos generales aplicables al evento.
     */
    @Column(length = 500)
    private String requisitos;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private ClubEntity club;

    /**
     * Fecha desde la cual el evento es visible públicamente.
     * En proyectos y convocatorias, se alinea con fechaInicioPostulacion.
     */
    @Column(nullable = false)
    private LocalDate fechaPublicacion;

    /**
     * Fecha en la cual deja de ser visible públicamente.
     * En proyectos y convocatorias, se alinea con fechaFinPostulacion.
     */
    @Column(nullable = false)
    private LocalDate fechaCierre;

    /**
     * Cupo máximo permitido.
     */
    private Integer cupoMaximo;

    /**
     * Cantidad actual de inscritos.
     */
    private Integer inscritos;

    /**
     * Estado general del evento.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEvento estado = EstadoEvento.ACTIVO;

    // ============================================================
    // MÉTODOS DE UTILIDAD
    // ============================================================

    /**
     * Sincroniza publicación/cierre con el periodo de postulación.
     * Usado por Convocatoria y Proyecto.
     */
    public void sincronizarConPeriodoPostulacion(LocalDate inicio, LocalDate fin) {
        this.fechaPublicacion = inicio;
        this.fechaCierre = fin;
    }

    /**
     * Marca el evento como cerrado.
     * No significa cancelación.
     */
    public void cerrarPublicacion() {
        this.estado = EstadoEvento.CERRADO;
    }

    public enum EstadoEvento {
        /**
         * Visible públicamente.
         */
        ACTIVO,

        /**
         * Ya no se muestra públicamente.
         */
        CERRADO,

        /**
         * Cancelado permanentemente.
         */
        CANCELADO
    }
}
