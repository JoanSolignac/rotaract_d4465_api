package com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces;


import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import org.springframework.data.domain.Page;

public interface IConvocatoriaService {
    Page<ConvocatoriaResponseDto> findAll(int page, int size);
    ConvocatoriaResponseDto findById(Long id);

    ConvocatoriaResponseDto create(ConvocatoriaCreateRequestDto dto);

    ConvocatoriaResponseDto update(Long id, ConvocatoriaEditRequestDto dto);


}
