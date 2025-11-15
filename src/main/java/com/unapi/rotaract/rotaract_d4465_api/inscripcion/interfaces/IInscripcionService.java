package com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import org.springframework.data.domain.Page;

public interface IInscripcionService {

    void inscribir(Long convocatoriaId);
    Page<InscripcionResponseDto> listarInscripciones(Long convocatoriaId, int page, int size);

    void aceptarInscripcion(Long inscripcionId);

    void rechazarInscripcion(Long inscripcionId);

}
