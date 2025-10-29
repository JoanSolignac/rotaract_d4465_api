package com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import org.hibernate.query.Page;

public interface IConvocatoriaService {

    Page<ConvocatoriaResponseDto> findAll(int page, int size);

    Page<ConvocatoriaResponseDto> findAllByClub(Long clubId, Integer page, Integer size);

    ConvocatoriaResponseDto findById(long id);

    ConvocatoriaResponseDto createConvocatoria(ConvocatoriaResponseDto convocatoriaDto);

    ConvocatoriaResponseDto updateConvocatoria(long id, ConvocatoriaEditRequestDto convocatoriaDto);

    ConvocatoriaResponseDto desactivateClub(long id);

    Page<ConvocatoriaResponseDto> FindAll(int page, int size);
}
