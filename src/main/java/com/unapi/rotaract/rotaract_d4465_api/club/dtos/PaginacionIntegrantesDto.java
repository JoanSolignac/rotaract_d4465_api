package com.unapi.rotaract.rotaract_d4465_api.club.dtos;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginacionIntegrantesDto {

    private List<UsuarioResumenDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
