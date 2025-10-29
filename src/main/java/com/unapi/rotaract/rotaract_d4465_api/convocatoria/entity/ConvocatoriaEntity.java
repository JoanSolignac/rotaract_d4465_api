package com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "convocatorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ConvocatoriaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String requisitos;

    @Column(nullable = false, length = 100)
    private String tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private ClubEntity club;

}
