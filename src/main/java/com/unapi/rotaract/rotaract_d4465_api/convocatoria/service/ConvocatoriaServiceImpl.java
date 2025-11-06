package com.unapi.rotaract.rotaract_d4465_api.convocatoria.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces.IConvocatoriaService;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import jakarta.validation.Valid;
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
    public Page<ConvocatoriaResponseDto> findAll(@Valid int page, @Valid int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        }

        List<ConvocatoriaResponseDto> convocatoriaList = convocatoriaRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(convocatoriaEntity -> ConvocatoriaResponseDto.builder()
                        .id(convocatoriaEntity.getId())
                        .nombreClub(convocatoriaEntity.getClub().getNombre())
                        .titulo(convocatoriaEntity.getTitulo())
                        .descripcion(convocatoriaEntity.getDescripcion())
                        .fechaInicio(convocatoriaEntity.getFechaInicio())
                        .fechaFin(convocatoriaEntity.getFechaFin())
                        .lugar(convocatoriaEntity.getLugar())
                        .requisitos(convocatoriaEntity.getRequisitos())
                        .activo(convocatoriaEntity.getActivo())
                        .build())
                .toList();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(convocatoriaList, pageable, convocatoriaList.size());

    }

    @Override
    public ConvocatoriaResponseDto findById(@Valid Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("El identificador debe ser mayor que 0.");
        }

        ConvocatoriaEntity convocatoriaEntity = convocatoriaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Convocatoria con id " + id + " no encontrada."));
        return ConvocatoriaResponseDto
                .builder()
                .id(convocatoriaEntity.getId())
                .nombreClub(convocatoriaEntity.getClub().getNombre())
                .titulo(convocatoriaEntity.getTitulo())
                .descripcion(convocatoriaEntity.getDescripcion())
                .fechaInicio(convocatoriaEntity.getFechaInicio())
                .fechaFin(convocatoriaEntity.getFechaFin())
                .lugar(convocatoriaEntity.getLugar())
                .requisitos(convocatoriaEntity.getRequisitos())
                .activo(convocatoriaEntity.getActivo())
                .build();
    }

    @Override
    public ConvocatoriaResponseDto create(@Valid ConvocatoriaCreateRequestDto convocatoriaCreateRequestDto) {

        if (convocatoriaCreateRequestDto.fechaFin().isBefore(convocatoriaCreateRequestDto.fechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(username).orElseThrow(() -> new IllegalArgumentException("Usuario con correo " + username + " no encontrado.")
        );

        ClubEntity clubEntity =  usuarioEntity.getClub();


        ConvocatoriaEntity convocatoriaEntity = convocatoriaRepository.save(
                ConvocatoriaEntity
                        .builder()
                        .titulo(convocatoriaCreateRequestDto.titulo())
                        .descripcion(convocatoriaCreateRequestDto.descripcion())
                        .fechaInicio(convocatoriaCreateRequestDto.fechaInicio())
                        .fechaFin(convocatoriaCreateRequestDto.fechaFin())
                        .lugar(convocatoriaCreateRequestDto.lugar())
                        .requisitos(convocatoriaCreateRequestDto.requisitos())
                        .club(clubEntity)
                        .activo(true)
                        .build()
        );
        return ConvocatoriaResponseDto
                .builder()
                .id(convocatoriaEntity.getId())
                .nombreClub(convocatoriaEntity.getClub().getNombre())
                .titulo(convocatoriaEntity.getTitulo())
                .descripcion(convocatoriaEntity.getDescripcion())
                .fechaInicio(convocatoriaEntity.getFechaInicio())
                .fechaFin(convocatoriaEntity.getFechaFin())
                .lugar(convocatoriaEntity.getLugar())
                .requisitos(convocatoriaEntity.getRequisitos())
                .activo(convocatoriaEntity.getActivo())
                .build();
    }

    @Override
    public ConvocatoriaResponseDto update(@Valid Long id, @Valid ConvocatoriaEditRequestDto convocatoriaEditRequestDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(username).orElseThrow(() -> new IllegalArgumentException("Usuario con correo " + username + " no encontrado.")
        );

        ConvocatoriaEntity convocatoriaEntity = convocatoriaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
        );

        if (!usuarioEntity.getClub().equals(convocatoriaEntity.getClub())) {
            throw new IllegalArgumentException("El usuario no tiene permiso para actualizar esta convocatoria.");
        }

        if (convocatoriaEditRequestDto.titulo() != null){
            convocatoriaEntity.setTitulo(convocatoriaEditRequestDto.titulo());
        }

        if (convocatoriaEditRequestDto.descripcion() != null){
            convocatoriaEntity.setDescripcion(convocatoriaEditRequestDto.descripcion());
        }

        if (convocatoriaEditRequestDto.fechaInicio() != null){
            convocatoriaEntity.setFechaInicio(convocatoriaEditRequestDto.fechaInicio());
        }

        if (convocatoriaEditRequestDto.fechaFin() != null){
            convocatoriaEntity.setFechaFin(convocatoriaEditRequestDto.fechaFin());
        }

        if (convocatoriaEditRequestDto.lugar() != null){
            convocatoriaEntity.setLugar(convocatoriaEditRequestDto.lugar());
        }

        if (convocatoriaEditRequestDto.requisitos() != null){
            convocatoriaEntity.setRequisitos(convocatoriaEditRequestDto.requisitos());
        }


        ConvocatoriaEntity updatedConvocatoria = convocatoriaRepository.save(convocatoriaEntity);

        return ConvocatoriaResponseDto
                .builder()
                .id(updatedConvocatoria.getId())
                .titulo(updatedConvocatoria.getTitulo())
                .descripcion(updatedConvocatoria.getDescripcion())
                .fechaInicio(updatedConvocatoria.getFechaInicio())
                .fechaFin(updatedConvocatoria.getFechaFin())
                .lugar(updatedConvocatoria.getLugar())
                .requisitos(updatedConvocatoria.getRequisitos())
                .build();
    }

}

