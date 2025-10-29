package com.unapi.rotaract.rotaract_d4465_api.convocatoria.service;

import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces.IConvocatoriaService;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class ConvocatoriaServiceImpl implements IConvocatoriaService {
    private final ConvocatoriaRepository convocatoriaRepository;
    private final ClubRepository clubRepository;

    @Override
    public Page<ConvocatoriaResponseDto> FindAll(int page, int size){
        if (size <= 0){
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        }

        List<ConvocatoriaResponseDto> clubList = ConvocatoriaRepository.findAll(PageRequest.of(page, size))
                .stream()
                .filter(c -> c.getActivo() == true)
                .map(
                        clubEntity -> ClubResponseDto.builder()
                                .id(clubEntity.getId())
                                .nombre(clubEntity.getNombre())
                                .departamento(clubEntity.getDepartamento())
                                .ciudad(clubEntity.getCiudad())
                                .fechaCreacion(clubEntity.getFechaCreacion())
                                .activo(clubEntity.getActivo())
                                .build()
                ).toList();

        Pageable pageable =  PageRequest.of(page, size);
        return new PageImpl<>(clubList, pageable, clubList.size());
    }
}
