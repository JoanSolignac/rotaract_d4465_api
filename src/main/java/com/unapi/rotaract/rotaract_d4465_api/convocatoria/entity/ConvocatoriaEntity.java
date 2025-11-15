package com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Representa una Convocatoria emitida por un Club Rotaract.
 *
 * Hereda todos los campos comunes de EventoEntity:
 * - titulo
 * - descripcion
 * - lugar
 * - requisitos
 * - fechaInicio / fechaFin
 * - activo
 *
 * Esta entidad define únicamente aquello que es exclusivo de una convocatoria:
 * su relación con un Club.
 */
@Entity
@Table(name = "convocatorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConvocatoriaEntity extends EventoEntity {

    /**
     * Club emisor de la convocatoria.
     * Relación muchos-a-uno con {@link ClubEntity}.
     *
     * Una convocatoria siempre pertenece a un club.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private ClubEntity club;
}
