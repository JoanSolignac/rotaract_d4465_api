package com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity;

import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "convocatorias")
@DiscriminatorValue("CONVOCATORIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConvocatoriaEntity extends EventoEntity {

    /**
     * Fecha desde la cual los usuarios pueden postular.
     */
    @Column(nullable = false)
    private LocalDate fechaInicioPostulacion;

    /**
     * Fecha límite de postulación.
     */
    @Column(nullable = false)
    private LocalDate fechaFinPostulacion;

    /**
     * Requisitos específicos de la convocatoria.
     */
    @Column(length = 500)
    private String requisitos;
}
