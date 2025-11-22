package com.unapi.rotaract.rotaract_d4465_api.proyecto.entity;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un proyecto dentro de la plataforma.
 * Extiende la clase base {@link EventoEntity}, heredando atributos
 * comunes como título, descripción, requisitos y lugar.
 *
 * Este módulo representa el ciclo de vida completo del proyecto:
 * postulación, ejecución, asistencia y finalización.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("PROYECTO")
@SuperBuilder
public class ProyectoEntity extends EventoEntity {

    // ============================================================
    // CAMPOS ESPECÍFICOS DEL PROYECTO
    // ============================================================

    @Column(nullable = false, length = 500)
    private String objetivo;

    @Column(nullable = false)
    private LocalDate fechaInicioPostulacion;

    @Column(nullable = false)
    private LocalDate fechaFinPostulacion;

    @Column(nullable = false)
    private LocalDate fechaInicioProyecto;

    @Column(nullable = false)
    private LocalDate fechaFinProyecto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProyecto estadoProyecto;

    // ============================================================
    // SISTEMA DE ASISTENCIA
    // ============================================================

    @Column(nullable = false)
    private Boolean asistenciaActiva = false;

    @Column(nullable = false)
    private Boolean asistenciaCerrada = false;

    @OneToMany(
            mappedBy = "proyecto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<AsistenciaEntity> asistencias = new ArrayList<>();


    // ============================================================
    // MÉTODOS DE UTILIDAD — CICLO DE VIDA Y ASISTENCIA
    // ============================================================

    /** ¿Está dentro del periodo de postulación? */
    public boolean estaEnPostulacion() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicioPostulacion) && !hoy.isAfter(fechaFinPostulacion);
    }

    /** ¿Está dentro de la etapa de ejecución del proyecto? */
    public boolean estaEnEjecucion() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicioProyecto) && !hoy.isAfter(fechaFinProyecto);
    }

    /** ¿El proyecto ya terminó? */
    public boolean haFinalizado() {
        return LocalDate.now().isAfter(fechaFinProyecto);
    }

    /** ¿Se permite registrar asistencia en este momento? */
    public boolean puedeRegistrarAsistencia() {
        return asistenciaActiva && !asistenciaCerrada && estaEnEjecucion();
    }

    /** Activar asistencia (cuando inicia ejecución) */
    public void activarAsistencia() {
        this.asistenciaActiva = true;
        this.asistenciaCerrada = false;
    }

    /** Cerrar asistencia (cuando finaliza ejecución) */
    public void cerrarAsistencia() {
        this.asistenciaActiva = false;
        this.asistenciaCerrada = true;
    }

    // ============================================================
    // ENUM DEL PROYECTO
    // ============================================================

    public enum EstadoProyecto {
        PROPUESTO,
        EN_POSTULACION,
        EN_EJECUCION,
        FINALIZADO,
        CANCELADO
    }
}
