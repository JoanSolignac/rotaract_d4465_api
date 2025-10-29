package com.unapi.rotaract.rotaract_d4465_api.convocatoria.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces.IConvocatoriaService;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class ConvocatoriaServiceImpl implements IConvocatoriaService {
    private final ConvocatoriaRepository convocatoriaRepository;
    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;


    @Override
    public Page<ConvocatoriaResponseDto> findAll(int page, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        }

        List<ConvocatoriaResponseDto> convocatoriaList = convocatoriaRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(convocatoriaEntity -> ConvocatoriaResponseDto.builder()
                        .id(convocatoriaEntity.getId())
                        .requisitos(convocatoriaEntity.getRequisitos())
                        .tipo(convocatoriaEntity.getTipo())
                        .build())
                .toList();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(convocatoriaList, pageable, convocatoriaList.size());

    }

    @Override
    public ConvocatoriaResponseDto findById(Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("El identificador debe ser mayor que 0.");
        }

        ConvocatoriaEntity convocatoriaEntity = convocatoriaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Convocatoria con id " + id + " no encontrada."));
        return ConvocatoriaResponseDto
                .builder()
                .id(convocatoriaEntity.getId())
                .build();
    }

    @Override
    public ConvocatoriaResponseDto create(ConvocatoriaCreateRequestDto convocatoriaDto) {
        if (convocatoriaDto == null ) {
            throw new IllegalArgumentException("El requisito no puede ser nulo.");
        }

        ConvocatoriaEntity convocatoriaEntity = convocatoriaRepository.save(
                ConvocatoriaEntity
                        .builder()
                        .requisitos(convocatoriaDto.requisitos())
                        .tipo(convocatoriaDto.tipo())
                        .build()
        );
        return ConvocatoriaResponseDto
                .builder()
                .id(convocatoriaEntity.getId())
                .requisitos(convocatoriaEntity.getRequisitos())
                .tipo(convocatoriaEntity.getTipo())
                .build();
    }

    @Override
    public ConvocatoriaResponseDto update(Long id, ConvocatoriaEditRequestDto convocatoriaDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsuarioEntity usuarioEntity = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario con id " + id + " no encontrado.")
        );

        ConvocatoriaEntity convocatoriaEntity = convocatoriaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
        );

        if (convocatoriaDto.requisitos() != null){
            convocatoriaEntity.setRequisitos(convocatoriaDto.requisitos());
        }
        if (convocatoriaDto.tipo() != null){
            convocatoriaEntity.setTipo(convocatoriaDto.tipo());
        }





        return ConvocatoriaResponseDto
                .builder()
                .id(convocatoriaEntity.getId())
                .requisitos(convocatoriaEntity.getRequisitos())
                .tipo(convocatoriaEntity.getTipo())
                .build();
    }
}

