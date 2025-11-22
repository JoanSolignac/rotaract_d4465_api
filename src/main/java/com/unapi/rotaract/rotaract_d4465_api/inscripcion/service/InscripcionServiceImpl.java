package com.unapi.rotaract.rotaract_d4465_api.inscripcion.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.MisInscripcionesItemDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio que gestiona todas las operaciones de inscripciones a convocatorias y proyectos.
 * Implementa:
 *  - Inscripción
 *  - Cancelación
 *  - Aprobación / rechazo
 *  - Listado por evento
 *  - Listado del usuario autenticado
 *
 * Esta clase NO crea repositorios, solo utiliza los ya existentes.
 */
@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements IInscripcionService {

    // ============================================================
    // DEPENDENCIAS
    // ============================================================

    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final ProyectoRepository proyectoRepository;

    // ============================================================
    // UTILIDAD: OBTENER USUARIO AUTENTICADO
    // ============================================================

    /**
     * Obtiene el usuario autenticado desde el SecurityContext.
     *
     * @return entidad UsuarioEntity del usuario autenticado.
     */
    private UsuarioEntity getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));
    }

    // ============================================================
    // INSCRIPCIÓN EN CONVOCATORIAS (ROL: INTERESADO)
    // ============================================================

    /**
     * Registra una inscripción a una convocatoria.
     * Solo usuarios con rol "INTERESADO" pueden inscribirse.
     *
     * @param convocatoriaId ID de la convocatoria objetivo
     */
    @Override
    @Transactional
    public void inscribirseEnConvocatoria(Long convocatoriaId) {

        UsuarioEntity usuario = getUsuarioAutenticado();
        String rolUsuario = usuario.getRol().getNombre();

        ConvocatoriaEntity convocatoria = convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Convocatoria no encontrada."));

        // Validación de cupo disponible
        if (convocatoria.getCupoMaximo() - convocatoria.getInscritos() <= 0) {
            throw new IllegalArgumentException("La convocatoria no tiene cupo disponible.");
        }

        // Solo INTERESADO puede inscribirse
        if (!rolUsuario.equalsIgnoreCase("INTERESADO")) {
            throw new IllegalArgumentException("Solo usuarios INTERESADO pueden inscribirse a convocatorias.");
        }

        // Validación de inscripción activa
        boolean tieneActiva = inscripcionRepository
                .existsByUsuarioIdAndConvocatoriaIsNotNullAndEstadoIn(
                        usuario.getId(),
                        List.of(
                                InscripcionEntity.EstadoInscripcion.PENDIENTE,
                                InscripcionEntity.EstadoInscripcion.ACEPTADA
                        )
                );

        if (tieneActiva) {
            throw new IllegalArgumentException("Ya tienes una inscripción activa a una convocatoria.");
        }

        // Crear inscripción
        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .convocatoria(convocatoria)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);

        // Incrementar inscritos
        convocatoria.setInscritos(convocatoria.getInscritos() + 1);
        convocatoriaRepository.save(convocatoria);
    }

    // ============================================================
    // INSCRIPCIÓN EN PROYECTOS (ROL: SOCIO / PRESIDENTE)
    // ============================================================

    /**
     * Registra la inscripción del usuario a un proyecto.
     * Roles permitidos: SOCIO, PRESIDENTE.
     *
     * @param proyectoId ID del proyecto objetivo
     */
    @Override
    @Transactional
    public void inscribirseEnProyecto(Long proyectoId) {

        UsuarioEntity usuario = getUsuarioAutenticado();
        String rolUsuario = usuario.getRol().getNombre();

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        // Validación por rol
        if (!(rolUsuario.equalsIgnoreCase("SOCIO") || rolUsuario.equalsIgnoreCase("PRESIDENTE"))) {
            throw new IllegalArgumentException("Solo SOCIOS o PRESIDENTES pueden inscribirse a proyectos.");
        }

        // Validar cupo
        if (proyecto.getCupoMaximo() - proyecto.getInscritos() <= 0) {
            throw new IllegalArgumentException("El proyecto no tiene cupo disponible.");
        }

        // Validar si ya está inscrito
        if (inscripcionRepository.existsByUsuarioIdAndProyectoId(usuario.getId(), proyectoId)) {
            throw new IllegalArgumentException("Ya estás inscrito en este proyecto.");
        }

        // Registrar inscripción
        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .proyecto(proyecto)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);
    }

    // ============================================================
    // CANCELAR INSCRIPCIÓN (CONVOCATORIAS)
    // ============================================================

    /**
     * Cancela la inscripción del usuario autenticado en una convocatoria.
     * Solo permitido si está en estado PENDIENTE.
     *
     * @param convocatoriaId ID de la convocatoria objetivo
     */
    @Override
    @Transactional
    public void cancelarInscripcionConvocatoria(Long convocatoriaId) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        // Buscar inscripción del usuario
        InscripcionEntity inscripcion = inscripcionRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(i -> i.getConvocatoria() != null &&
                        i.getConvocatoria().getId().equals(convocatoriaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró inscripción del usuario en esta convocatoria."
                ));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar inscripciones en estado PENDIENTE.");
        }

        // Actualizar inscritos
        ConvocatoriaEntity convocatoria = inscripcion.getConvocatoria();
        if (convocatoria.getInscritos() != null && convocatoria.getInscritos() > 0) {
            convocatoria.setInscritos(convocatoria.getInscritos() - 1);
            convocatoriaRepository.save(convocatoria);
        }

        inscripcionRepository.delete(inscripcion);
    }

    // ============================================================
    // CANCELAR INSCRIPCIÓN (PROYECTOS)
    // ============================================================

    /**
     * Cancela la inscripción del usuario autenticado en un proyecto.
     * Solo permitido si está en estado PENDIENTE.
     *
     * @param proyectoId ID del proyecto objetivo
     */
    @Override
    @Transactional
    public void cancelarInscripcionProyecto(Long proyectoId) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        // Buscar inscripción del usuario
        InscripcionEntity inscripcion = inscripcionRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(i -> i.getProyecto() != null &&
                        i.getProyecto().getId().equals(proyectoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró inscripción del usuario en este proyecto."
                ));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar inscripciones en estado PENDIENTE.");
        }

        // No se afecta 'inscritos' porque solo aumenta al ACEPTAR
        inscripcionRepository.delete(inscripcion);
    }

    // ============================================================
    // ACEPTAR INSCRIPCIÓN (PRESIDENTE)
    // ============================================================

    /**
     * Acepta una inscripción, cambiando su estado y aplicando las reglas
     * dependiendo de si es convocatoria o proyecto.
     *
     * @param inscripcionId ID de la inscripción
     */
    @Override
    @Transactional
    public void aceptarInscripcion(Long inscripcionId) {

        InscripcionEntity insc = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        insc.setEstado(InscripcionEntity.EstadoInscripcion.ACEPTADA);

        // Caso: convocatoria
        if (insc.getConvocatoria() != null) {

            UsuarioEntity usuario = insc.getUsuario();
            usuario.setClub(insc.getConvocatoria().getClub());

            RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new IllegalArgumentException("Rol SOCIO no encontrado."));

            usuario.setRol(rolSocio);
            usuarioRepository.save(usuario);
            inscripcionRepository.save(insc);
            return;
        }

        // Caso: proyecto
        if (insc.getProyecto() != null) {

            ProyectoEntity proyecto = proyectoRepository.findById(insc.getProyecto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

            proyecto.setInscritos(proyecto.getInscritos() + 1);

            proyectoRepository.save(proyecto);
            inscripcionRepository.save(insc);
            return;
        }

        // Caso: error de integridad
        throw new IllegalStateException("La inscripción no pertenece a ninguna convocatoria ni proyecto.");
    }

    // ============================================================
    // RECHAZAR INSCRIPCIÓN
    // ============================================================

    /**
     * Rechaza una inscripción, aplicando reglas de decremento de cupo
     * solo si pertenece a un proyecto.
     *
     * @param inscripcionId ID de la inscripción
     */
    @Override
    @Transactional
    public void rechazarInscripcion(Long inscripcionId) {

        InscripcionEntity insc = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        insc.setEstado(InscripcionEntity.EstadoInscripcion.RECHAZADA);

        // Proyecto: decrementar cupo solo si estaba aceptada
        if (insc.getProyecto() != null) {
            ProyectoEntity proyecto = proyectoRepository.findById(insc.getProyecto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

            proyecto.setInscritos(proyecto.getInscritos() - 1);
            proyectoRepository.save(proyecto);
        }

        inscripcionRepository.save(insc);
    }

    // ============================================================
    // LISTADOS POR EVENTO
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDto> listarInscripcionesConvocatoria(Long convocatoriaId, int page, int size) {
        var pageEntities = inscripcionRepository.findByConvocatoriaId(convocatoriaId, PageRequest.of(page, size));
        return pageEntities.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDto> listarInscripcionesProyecto(Long proyectoId, int page, int size) {
        var pageEntities = inscripcionRepository.findByProyectoId(proyectoId, PageRequest.of(page, size));
        return pageEntities.map(this::mapToResponse);
    }

    // ============================================================
    // LISTADO DEL USUARIO AUTENTICADO
    // ============================================================

    /**
     * Devuelve un listado unificado de todas las inscripciones del usuario autenticado.
     *
     * @return lista de DTOs MisInscripcionesItemDto
     */
    @Override
    @Transactional(readOnly = true)
    public List<MisInscripcionesItemDto> obtenerMisInscripciones() {

        UsuarioEntity usuario = getUsuarioAutenticado();

        List<InscripcionEntity> inscripciones = inscripcionRepository.findByUsuarioId(usuario.getId());

        return inscripciones.stream()
                .map(this::mapToMisInscripcionesItem)
                .toList();
    }

    // ============================================================
    // MAPPERS
    // ============================================================

    /**
     * Convierte una entidad InscripcionEntity a InscripcionResponseDto.
     */
    private InscripcionResponseDto mapToResponse(InscripcionEntity insc) {

        String tipo;
        Long referenciaId;
        String referenciaTitulo;

        if (insc.getConvocatoria() != null) {
            tipo = "CONVOCATORIA";
            referenciaId = insc.getConvocatoria().getId();
            referenciaTitulo = insc.getConvocatoria().getTitulo();
        } else {
            tipo = "PROYECTO";
            referenciaId = insc.getProyecto().getId();
            referenciaTitulo = insc.getProyecto().getTitulo();
        }

        UsuarioEntity u = insc.getUsuario();

        return new InscripcionResponseDto(
                insc.getId(),
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                tipo,
                referenciaId,
                referenciaTitulo,
                insc.getEstado().name(),
                insc.getFechaRegistro()
        );
    }

    /**
     * Convierte una inscripción a un DTO compacto para el usuario autenticado.
     */
    private MisInscripcionesItemDto mapToMisInscripcionesItem(InscripcionEntity insc) {

        String tipoEvento;
        Long eventoId;
        String tituloEvento;
        String clubNombre = null;

        if (insc.getConvocatoria() != null) {
            var c = insc.getConvocatoria();
            tipoEvento = "CONVOCATORIA";
            eventoId = c.getId();
            tituloEvento = c.getTitulo();
            if (c.getClub() != null) {
                clubNombre = c.getClub().getNombre();
            }
        } else if (insc.getProyecto() != null) {
            var p = insc.getProyecto();
            tipoEvento = "PROYECTO";
            eventoId = p.getId();
            tituloEvento = p.getTitulo();
            if (p.getClub() != null) {
                clubNombre = p.getClub().getNombre();
            }
        } else {
            tipoEvento = "DESCONOCIDO";
            eventoId = null;
            tituloEvento = null;
        }

        return new MisInscripcionesItemDto(
                insc.getId(),
                tipoEvento,
                eventoId,
                tituloEvento,
                clubNombre,
                insc.getEstado() != null ? insc.getEstado().name() : null,
                insc.getFechaRegistro()
        );
    }

}
