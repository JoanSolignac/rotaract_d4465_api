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

/**
 * Implementación de IConvocatoriaService para operaciones sobre convocatorias.
 * Proporciona lectura paginada, consulta individual, creación y actualización parcial.
 * Convenciones:
 * - page es índice 0-based (0 = primera página).
 * - size debe ser mayor que 0; de lo contrario se lanza IllegalArgumentException.
 * - Los métodos devuelven DTOs (ConvocatoriaResponseDto); nunca null.
 * - El método update aplica sólo campos no nulos del DTO de edición.
 * Seguridad:
 * - create y update obtienen el usuario autenticado del SecurityContext y validan pertenencia al club.
 */
@Service
@RequiredArgsConstructor

public class ConvocatoriaServiceImpl implements IConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;


    /**
     * Recupera una página de convocatorias.
     * Construye un Pageable con los parámetros y transforma las entidades en ConvocatoriaResponseDto.
     * No filtra por estado (incluye activas e inactivas según lo que retorne el repositorio).
     * @param page índice de página (0-based)
     * @param size tamaño máximo de elementos por página (debe ser > 0)
     * @return Page de ConvocatoriaResponseDto correspondiente a la página solicitada (puede estar vacía)
     * @throws IllegalArgumentException si size <= 0
     */
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

    /**
     * Recupera una convocatoria por su identificador.
     * Delegado a ConvocatoriaRepository#findById; transforma la entidad en ConvocatoriaResponseDto.
     * @param id identificador único (> 0)
     * @return ConvocatoriaResponseDto con los datos de la convocatoria
     * @throws IllegalArgumentException si id < 0 o si la convocatoria no existe
     */
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

    /**
     * Crea una nueva convocatoria asociada al club del usuario autenticado.
     * Valida que fechaFin no sea anterior a fechaInicio.
     * @param convocatoriaCreateRequestDto DTO con datos obligatorios de creación
     * @return ConvocatoriaResponseDto representando la convocatoria persistida
     * @throws IllegalArgumentException si fechas inválidas o usuario no encontrado
     */
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

    /**
     * Actualiza parcialmente una convocatoria existente.
     * Valida que el usuario autenticado pertenezca al mismo club de la convocatoria.
     * Aplica sólo campos no nulos del DTO de edición.
     * @param id identificador de la convocatoria a actualizar
     * @param convocatoriaEditRequestDto DTO con campos editables (puede contener nulos)
     * @return ConvocatoriaResponseDto con el estado actualizado
     * @throws IllegalArgumentException si usuario sin permiso, convocatoria inexistente o datos inválidos
     */
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
