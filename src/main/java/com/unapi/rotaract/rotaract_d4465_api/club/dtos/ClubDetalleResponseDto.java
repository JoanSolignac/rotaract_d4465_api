package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubDetalleResponseDto {

    private ClubResumenDto club;
    private UsuarioResumenDto presidente;
    private ClubMetricasDto metricas;
    private PaginacionIntegrantesDto integrantes;
    private List<ConvocatoriaResumenDto> convocatorias;
    private List<ProyectoResumenDto> proyectos;
}
