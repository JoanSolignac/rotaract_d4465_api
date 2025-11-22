package com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity;

import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Entidad JPA que representa una convocatoria dentro de la plataforma.
 *
 * Hereda de {@link EventoEntity}, el cual maneja:
 * - título
 * - descripción
 * - requisitos generales
 * - lugar
 * - relación con club
 * - fechaPublicacion (inicio postulación)
 * - fechaCierre (fin postulación)
 * - cupos
 * - estado general de visibilidad
 *
 * Esta entidad define únicamente los rangos de postulación,
 * asegurando simplicidad y coherencia con el modelo general.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "convocatorias")
@DiscriminatorValue("CONVOCATORIA")
@SuperBuilder
public class ConvocatoriaEntity extends EventoEntity {

    /**
     * Fecha desde la cual los usuarios pueden postular.
     * Mantiene coherencia con EventoEntity.fechaPublicacion.
     */
    @Column(nullable = false)
    private LocalDate fechaInicioPostulacion;

    /**
     * Fecha límite de postulación.
     * Mantiene coherencia con EventoEntity.fechaCierre.
     */
    @Column(nullable = false)
    private LocalDate fechaFinPostulacion;

    // ============================================================
    // MÉTODOS DE UTILIDAD
    // ============================================================

    /** ¿La convocatoria está en periodo de postulación? */
    public boolean estaEnPostulacion() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicioPostulacion) && !hoy.isAfter(fechaFinPostulacion);
    }

    /** ¿El periodo de postulación ya terminó? */
    public boolean haFinalizadoPostulacion() {
        return LocalDate.now().isAfter(fechaFinPostulacion);
    }
}
