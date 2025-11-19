package com.unapi.rotaract.rotaract_d4465_api.evento.entity;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

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
     * Lugar donde se ejecutará el evento (proyecto o convocatoria).
     */
    @Column(nullable = true)
    private String lugar;

    /**
     * Requisitos para poder participar del evento.
     */
    @Column(nullable = true, length = 500)
    private String requisitos;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private ClubEntity club;

    @Column(nullable = false)
    private LocalDate fechaPublicacion;

    @Column(nullable = false)
    private LocalDate fechaCierre;

    private Integer cupoMaximo;

    private Integer inscritos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEvento estado = EstadoEvento.ACTIVO;

    public enum EstadoEvento {
        ACTIVO,
        CERRADO,
        CANCELADO
    }
}
