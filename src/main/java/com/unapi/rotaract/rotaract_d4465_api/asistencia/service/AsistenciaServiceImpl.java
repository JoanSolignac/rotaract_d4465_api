package com.unapi.rotaract.rotaract_d4465_api.asistencia.service;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaGuardarRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.interfaces.IAsistenciaService;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.repository.AsistenciaRepository;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de gestión de asistencias a proyectos.
 * - Lista inscritos para marcar asistencia
 * - Guarda presentes y faltas
 */
@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements IAsistenciaService {

    private final ProyectoRepository proyectoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;

    // ============================================================
    // LISTAR SOCIOS PARA ASISTENCIA
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarSociosParaAsistencia(Long proyectoId) {

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        if (!proyecto.puedeRegistrarAsistencia()) {
            throw new IllegalStateException("La asistencia no está activa para este proyecto.");
        }

        // Inscritos del proyecto
        List<InscripcionEntity> inscripciones =
                inscripcionRepository.findByProyectoId(proyectoId);

        return inscripciones.stream().map(ins -> {

            Long usuarioId = ins.getUsuario().getId();

            // Ver si ya tiene registro de asistencia
            AsistenciaEntity asistencia = asistenciaRepository
                    .findByProyectoIdAndUsuarioId(proyectoId, usuarioId)
                    .orElse(null);

            boolean presente = asistencia != null &&
                    asistencia.getEstado() == AsistenciaEntity.EstadoAsistencia.PRESENTE;

            UsuarioEntity u = ins.getUsuario();

            return AsistenciaResponseDto.builder()
                    .usuarioId(u.getId())
                    .usuarioNombreCompleto(u.getNombre())
                    .usuarioCorreo(u.getCorreo())
                    .proyectoId(proyectoId)
                    .presente(presente)
                    .build();

        }).toList();
    }

    // ============================================================
    // GUARDAR ASISTENCIA
    // ============================================================

    @Override
    @Transactional
    public void guardarAsistencia(Long proyectoId, AsistenciaGuardarRequestDto dto) {

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        if (!proyecto.puedeRegistrarAsistencia()) {
            throw new IllegalStateException("La asistencia no está activa para este proyecto.");
        }

        List<Long> presentesIds = dto.usuariosPresentesIds();

        // Inscritos reales del proyecto
        List<InscripcionEntity> inscripciones =
                inscripcionRepository.findByProyectoId(proyectoId);

        for (InscripcionEntity ins : inscripciones) {

            Long usuarioId = ins.getUsuario().getId();
            boolean esPresente = presentesIds.contains(usuarioId);

            AsistenciaEntity asistencia = asistenciaRepository
                    .findByProyectoIdAndUsuarioId(proyectoId, usuarioId)
                    .orElse(null);

            if (asistencia == null) {
                asistencia = AsistenciaEntity.builder()
                        .usuario(ins.getUsuario())
                        .proyecto(proyecto)
                        .estado(esPresente
                                ? AsistenciaEntity.EstadoAsistencia.PRESENTE
                                : AsistenciaEntity.EstadoAsistencia.FALTA)
                        .fechaRegistro(LocalDateTime.now())
                        .build();
            } else {
                asistencia.setEstado(esPresente
                        ? AsistenciaEntity.EstadoAsistencia.PRESENTE
                        : AsistenciaEntity.EstadoAsistencia.FALTA);
                asistencia.setFechaRegistro(LocalDateTime.now());
            }

            asistenciaRepository.save(asistencia);
        }
    }
}
