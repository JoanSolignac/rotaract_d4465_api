package com.unapi.rotaract.rotaract_d4465_api.asistencia.service;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaGuardarRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.interfaces.IAsistenciaService;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.repository.AsistenciaRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de gestión de asistencias a proyectos.
 *
 * Funcionalidades:
 * - Listar inscritos para marcar asistencia.
 * - Guardar presentes y faltas.
 * - Listar asistencias registradas de un proyecto.
 * - Obtener el historial de asistencias del usuario autenticado.
 */
@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements IAsistenciaService {

    private final ProyectoRepository proyectoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    // ============================================================
    // UTILIDAD: USUARIO AUTENTICADO
    // ============================================================

    /**
     * Obtiene el usuario actualmente autenticado a partir del contexto de seguridad.
     *
     * @return entidad {@link UsuarioEntity} del usuario autenticado
     * @throws IllegalArgumentException si no se encuentra el usuario en la base de datos
     */
    private UsuarioEntity getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));
    }

    // ============================================================
    // LISTAR SOCIOS PARA ASISTENCIA (PANTALLA DE MARCADO)
    // ============================================================

    /**
     * Lista los socios inscritos en un proyecto para que el presidente pueda
     * marcar asistencia. Si la asistencia no está activa, se lanza una excepción.
     */
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
    // GUARDAR ASISTENCIA (PRESENTES Y FALTAS)
    // ============================================================

    /**
     * Registra la asistencia de los usuarios inscritos a un proyecto.
     * Los IDs contenidos en {@code dto.usuariosPresentesIds()} se marcan como PRESENTE
     * y el resto de inscritos como FALTA.
     */
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

    // ============================================================
    // LISTAR ASISTENCIAS REGISTRADAS DE UN PROYECTO
    // ============================================================

    /**
     * Devuelve todas las asistencias registradas para un proyecto, independientemente
     * de si son presentes o faltas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarAsistenciasDeProyecto(Long proyectoId) {

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        List<AsistenciaEntity> asistencias = asistenciaRepository.findByProyectoId(proyectoId);

        return asistencias.stream()
                .map(this::mapAsistenciaToResponse)
                .toList();
    }

    // ============================================================
    // HISTORIAL DE MIS ASISTENCIAS (USUARIO AUTENTICADO)
    // ============================================================

    /**
     * Devuelve el historial de asistencias del usuario autenticado.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> obtenerMisAsistencias() {

        UsuarioEntity usuario = getUsuarioAutenticado();

        List<AsistenciaEntity> asistencias =
                asistenciaRepository.findByUsuarioId(usuario.getId());

        return asistencias.stream()
                .map(this::mapMisAsistencias)
                .toList();
    }


    // ============================================================
    // MAPPER AUXILIAR
    // ============================================================

    /**
     * Transforma una entidad de asistencia en su DTO de respuesta.
     */
    private AsistenciaResponseDto mapAsistenciaToResponse(AsistenciaEntity asistencia) {

        UsuarioEntity u = asistencia.getUsuario();
        Long proyectoId = asistencia.getProyecto() != null
                ? asistencia.getProyecto().getId()
                : null;

        boolean presente =
                asistencia.getEstado() == AsistenciaEntity.EstadoAsistencia.PRESENTE;

        return AsistenciaResponseDto.builder()
                .usuarioId(u.getId())
                .usuarioNombreCompleto(u.getNombre())
                .usuarioCorreo(u.getCorreo())
                .proyectoId(proyectoId)
                .presente(presente)
                .build();
    }

    private AsistenciaResponseDto mapMisAsistencias(AsistenciaEntity asistencia) {
        ProyectoEntity p = asistencia.getProyecto();

        boolean presente = asistencia.getEstado() ==
                AsistenciaEntity.EstadoAsistencia.PRESENTE;

        return AsistenciaResponseDto.builder()
                .usuarioId(null)
                .usuarioNombreCompleto(null)
                .usuarioCorreo(null)
                .proyectoId(p != null ? p.getId() : null)
                .presente(presente)
                .build();
    }

}
