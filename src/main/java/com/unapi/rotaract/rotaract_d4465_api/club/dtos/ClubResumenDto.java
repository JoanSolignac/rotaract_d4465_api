package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import lombok.*;
import java.time.LocalDate;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ClubResumenDto {
    private Long id;
    private String nombre;
    private String departamento;
    private String ciudad;
    private LocalDate fechaCreacion;
    private Boolean activo;
}
