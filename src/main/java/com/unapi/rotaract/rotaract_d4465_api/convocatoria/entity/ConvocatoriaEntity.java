package com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "convocatorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ClubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
}
