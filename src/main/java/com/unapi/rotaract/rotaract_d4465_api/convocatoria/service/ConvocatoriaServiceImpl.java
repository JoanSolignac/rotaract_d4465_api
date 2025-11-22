package com.unapi.rotaract.rotaract_d4465_api.convocatoria.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces.IConvocatoriaService;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio responsable de gestionar el ciclo de vida de las convocatorias.
 */
@Service
@RequiredArgsConstructor
public class ConvocatoriaServiceImpl implements IConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;

    // ============================================================
    // LISTADO GENERAL
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ConvocatoriaResponseDto> findAll(int page, int size) {
        return convocatoriaRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // ============================================================
    // LISTADO SOLO DEL PRESIDENTE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ConvocatoriaResponseDto> findAllByPresidente(int page, int size) {

        String correo = SecurityContextHolder.getContext().getAuthentication().getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        ClubEntity club = usuario.getClub();
        if (club == null) {
            throw new IllegalStateException("El usuario no pertenece a ningún club.");
        }

        return convocatoriaRepository.findByClubId(club.getId(), PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // ============================================================
    // NUEVO: LISTAR DISPONIBLES PARA INTERESADO
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ConvocatoriaResponseDto> findDisponiblesParaInteresado(int page, int size) {

        String correo = SecurityContextHolder.getContext().getAuthentication().getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        Long userId = usuario.getId();
        LocalDate hoy = LocalDate.now();

        // 1. Obtener convocatorias ACTIVAS y en fecha de postulación
        List<ConvocatoriaEntity> activas = convocatoriaRepository
                .findByEstado(EventoEntity.EstadoEvento.ACTIVO)
                .stream()
                .filter(c -> !hoy.isBefore(c.getFechaInicioPostulacion()) &&
                        !hoy.isAfter(c.getFechaFinPostulacion()))
                .toList();

        // 2. Obtener IDs de convocatorias donde el usuario YA está inscrito
        List<Long> idsInscritos = inscripcionRepository
                .findByUsuarioId(userId)
                .stream()
                .filter(i -> i.getConvocatoria() != null)
                .filter(i ->
                        i.getEstado() == InscripcionEntity.EstadoInscripcion.PENDIENTE ||
                                i.getEstado() == InscripcionEntity.EstadoInscripcion.ACEPTADA
                )
                .map(i -> i.getConvocatoria().getId())
                .toList();

        // 3. Filtrar convocatorias donde NO esté inscrito
        List<ConvocatoriaResponseDto> disponibles = activas.stream()
                .filter(c -> !idsInscritos.contains(c.getId()))
                .map(this::mapToResponse)
                .toList();

        // 4. Paginación manual
        int start = page * size;
        int end = Math.min(start + size, disponibles.size());

        if (start > disponibles.size()) {
            return Page.empty();
        }

        return new PageImpl<>(
                disponibles.subList(start, end),
                PageRequest.of(page, size),
                disponibles.size()
        );
    }

    // ============================================================
    // OBTENER POR ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaResponseDto findById(Long id) {

        ConvocatoriaEntity convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Convocatoria con id " + id + " no encontrada.")
                );

        return mapToResponse(convocatoria);
    }

    // ============================================================
    // CREAR
    // ============================================================

    @Override
    @Transactional
    public ConvocatoriaResponseDto create(ConvocatoriaCreateRequestDto dto) {

        String correo = SecurityContextHolder.getContext().getAuthentication().getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        ClubEntity club = usuario.getClub();
        if (club == null) {
            throw new IllegalStateException("El usuario no pertenece a un club.");
        }

        validarFechasCreacion(dto);

        ConvocatoriaEntity entity = ConvocatoriaEntity.builder()
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .club(club)
                .cupoMaximo(dto.cupoMaximo())
                .inscritos(0)
                .fechaPublicacion(dto.fechaInicioPostulacion())
                .fechaCierre(dto.fechaFinPostulacion())
                .fechaInicioPostulacion(dto.fechaInicioPostulacion())
                .fechaFinPostulacion(dto.fechaFinPostulacion())
                .requisitos(dto.requisitos())
                .estado(EventoEntity.EstadoEvento.ACTIVO)
                .build();

        return mapToResponse(convocatoriaRepository.save(entity));
    }

    // ============================================================
    // EDITAR
    // ============================================================

    @Override
    @Transactional
    public ConvocatoriaResponseDto update(Long id, ConvocatoriaEditRequestDto dto) {

        ConvocatoriaEntity entity = convocatoriaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Convocatoria con id " + id + " no encontrada.")
                );

        if (dto.titulo() != null) entity.setTitulo(dto.titulo());
        if (dto.descripcion() != null) entity.setDescripcion(dto.descripcion());
        if (dto.cupoMaximo() != null) entity.setCupoMaximo(dto.cupoMaximo());

        if (dto.fechaInicioPostulacion() != null) {
            entity.setFechaInicioPostulacion(dto.fechaInicioPostulacion());
            entity.setFechaPublicacion(dto.fechaInicioPostulacion());
        }
        if (dto.fechaFinPostulacion() != null) {
            entity.setFechaFinPostulacion(dto.fechaFinPostulacion());
            entity.setFechaCierre(dto.fechaFinPostulacion());
        }

        if (dto.requisitos() != null) entity.setRequisitos(dto.requisitos());

        validarFechasEdicion(entity);

        return mapToResponse(convocatoriaRepository.save(entity));
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================

    private void validarFechasCreacion(ConvocatoriaCreateRequestDto dto) {

        if (dto.fechaFinPostulacion().isBefore(dto.fechaInicioPostulacion())) {
            throw new IllegalArgumentException("La fecha fin de postulación no puede ser anterior a la fecha de inicio.");
        }

        if (dto.fechaFinPostulacion().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La convocatoria no puede finalizar en el pasado.");
        }
    }

    private void validarFechasEdicion(ConvocatoriaEntity entity) {

        if (entity.getFechaFinPostulacion().isBefore(entity.getFechaInicioPostulacion())) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha de inicio.");
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private ConvocatoriaResponseDto mapToResponse(ConvocatoriaEntity c) {
        return new ConvocatoriaResponseDto(
                c.getId(),
                c.getTitulo(),
                c.getDescripcion(),
                c.getRequisitos(),
                c.getCupoMaximo(),
                c.getInscritos(),
                c.getFechaPublicacion(),
                c.getFechaCierre(),
                c.getFechaInicioPostulacion(),
                c.getFechaFinPostulacion(),
                c.getClub().getId(),
                c.getClub().getNombre(),
                c.getEstado().name()
        );
    }
}
