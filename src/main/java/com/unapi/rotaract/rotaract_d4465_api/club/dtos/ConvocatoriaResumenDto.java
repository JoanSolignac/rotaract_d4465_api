package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConvocatoriaResumenDto {

    private Long id;
    private String titulo;
    private String estado;
    private LocalDate fechaInicioPostulacion;
    private LocalDate fechaFinPostulacion;
}
