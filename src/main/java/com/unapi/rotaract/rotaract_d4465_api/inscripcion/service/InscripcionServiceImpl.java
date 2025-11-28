package com.unapi.rotaract.rotaract_d4465_api.inscripcion.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
import com.unapi.rotaract.rotaract_d4465_api.common.services.NotificacionService;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements IInscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    private final ConvocatoriaRepository convocatoriaRepository;
    private final ProyectoRepository proyectoRepository;

    private final IEmailService emailService;
    private final NotificacionService notificacionService;

    /**
     * Obtiene el usuario autenticado desde el contexto de seguridad.
     */
    private UsuarioEntity getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));
    }

    // ============================================================
    // INSCRIPCIÓN EN CONVOCATORIAS (INTERESADO)
    // ============================================================

    @Override
    @Transactional
    public void inscribirseEnConvocatoria(Long convocatoriaId) {

        UsuarioEntity usuario = getUsuarioAutenticado();
        ConvocatoriaEntity convocatoria = convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Convocatoria no encontrada."));

        if (!usuario.getRol().getNombre().equals("INTERESADO"))
            throw new IllegalArgumentException("Solo los usuarios INTERESADO pueden inscribirse.");

        if (convocatoria.getCupoMaximo() - convocatoria.getInscritos() <= 0)
            throw new IllegalArgumentException("La convocatoria no tiene cupo disponible.");

        boolean tieneActiva = inscripcionRepository
                .existsByUsuarioIdAndConvocatoriaIsNotNullAndEstadoIn(
                        usuario.getId(),
                        List.of(
                                InscripcionEntity.EstadoInscripcion.PENDIENTE,
                                InscripcionEntity.EstadoInscripcion.ACEPTADA
                        )
                );

        if (tieneActiva)
            throw new IllegalArgumentException("Ya tienes una inscripción activa.");

        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .convocatoria(convocatoria)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);

        convocatoria.setInscritos(convocatoria.getInscritos() + 1);
        convocatoriaRepository.save(convocatoria);

        // --- NOTIFICACIÓN WEBSOCKET AL PRESIDENTE ---
        if (convocatoria.getClub() != null) {
            notificarPresidente(
                    convocatoria.getClub(),
                    "Nueva inscripción: " + usuario.getNombre() + " se ha postulado a '" + convocatoria.getTitulo() + "'"
            );
        }

        // --- NOTIFICACIÓN WEBSOCKET AL INTERESADO (CONFIRMACIÓN) ---
        notificacionService.enviarAUsuario(
                usuario.getId(),
                Map.of(
                        "titulo", "Solicitud Enviada",
                        "mensaje", "Tu solicitud para la convocatoria '" + convocatoria.getTitulo() + "' ha sido enviada exitosamente.",
                        "tipo", "INFO"
                )
        );
    }

    // ============================================================
    // INSCRIPCIÓN EN PROYECTOS (SOCIO / PRESIDENTE)
    // ============================================================

    @Override
    @Transactional
    public void inscribirseEnProyecto(Long proyectoId) {

        UsuarioEntity usuario = getUsuarioAutenticado();
        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        if (!(usuario.getRol().getNombre().equals("SOCIO") ||
                usuario.getRol().getNombre().equals("PRESIDENTE")))
            throw new IllegalArgumentException("Solo SOCIO y PRESIDENTE pueden inscribirse en proyectos.");

        if (proyecto.getCupoMaximo() - proyecto.getInscritos() <= 0)
            throw new IllegalArgumentException("El proyecto no tiene cupo disponible.");

        if (usuario.getClub() == null ||
                proyecto.getClub() == null ||
                !usuario.getClub().getId().equals(proyecto.getClub().getId()))
            throw new IllegalArgumentException("Solo puedes inscribirte en proyectos de tu club.");

        if (inscripcionRepository.existsByUsuarioIdAndProyectoId(usuario.getId(), proyectoId))
            throw new IllegalArgumentException("Ya estás inscrito en este proyecto.");

        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .proyecto(proyecto)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);

        proyecto.setInscritos(proyecto.getInscritos() + 1);
        proyectoRepository.save(proyecto);

        // --- NOTIFICACIÓN WEBSOCKET AL PRESIDENTE ---
        if (proyecto.getClub() != null) {
            notificarPresidente(
                    proyecto.getClub(),
                    "El socio " + usuario.getNombre() + " se inscribió al proyecto '" + proyecto.getTitulo() + "'"
            );
        }

        // --- NOTIFICACIÓN WEBSOCKET AL USUARIO (CONFIRMACIÓN) ---
        notificacionService.enviarAUsuario(
                usuario.getId(),
                Map.of(
                        "titulo", "Inscripción Exitosa",
                        "mensaje", "Te has inscrito correctamente al proyecto '" + proyecto.getTitulo() + "'.",
                        "tipo", "INFO"
                )
        );
    }

    // ============================================================
    // MÉTODO AUXILIAR PARA NOTIFICAR AL PRESIDENTE
    // ============================================================
    private void notificarPresidente(com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity club, String mensaje) {
        List<UsuarioEntity> miembros = usuarioRepository.findByClubId(club.getId());

        for (UsuarioEntity miembro : miembros) {
            if (miembro.getRol() != null && "PRESIDENTE".equalsIgnoreCase(miembro.getRol().getNombre())) {
                notificacionService.enviarAUsuario(
                        miembro.getId(),
                        Map.of(
                                "titulo", "Nueva Actividad",
                                "mensaje", mensaje,
                                "tipo", "ALERTA"
                        )
                );
            }
        }
    }

    // ============================================================
    // CANCELAR INSCRIPCIÓN EN CONVOCATORIAS
    // ============================================================

    @Override
    @Transactional
    public void cancelarInscripcionConvocatoria(Long convocatoriaId) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        InscripcionEntity inscripcion = inscripcionRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(i -> i.getConvocatoria() != null &&
                        i.getConvocatoria().getId().equals(convocatoriaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE)
            throw new IllegalStateException("Solo se puede cancelar una inscripción pendiente.");

        ConvocatoriaEntity convocatoria = inscripcion.getConvocatoria();
        convocatoria.setInscritos(convocatoria.getInscritos() - 1);
        convocatoriaRepository.save(convocatoria);

        inscripcionRepository.delete(inscripcion);
    }

    // ============================================================
    // CANCELAR INSCRIPCIÓN EN PROYECTOS
    // ============================================================

    @Override
    @Transactional
    public void cancelarInscripcionProyecto(Long proyectoId) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        InscripcionEntity inscripcion = inscripcionRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(i -> i.getProyecto() != null &&
                        i.getProyecto().getId().equals(proyectoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE)
            throw new IllegalStateException("Solo se puede cancelar inscripciones pendientes.");

        ProyectoEntity proyecto = inscripcion.getProyecto();
        proyecto.setInscritos(proyecto.getInscritos() - 1);
        proyectoRepository.save(proyecto);

        inscripcionRepository.delete(inscripcion);
    }

    // ============================================================
    // ACEPTAR INSCRIPCIÓN (MODIFICADO PARA JSON ESTRUCTURADO)
    // ============================================================

    @Override
    @Transactional
    public void aceptarInscripcion(Long inscripcionId) {

        InscripcionEntity insc = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        UsuarioEntity usuarioInscrito = insc.getUsuario();
        UsuarioEntity presidente = getUsuarioAutenticado();
        insc.setEstado(InscripcionEntity.EstadoInscripcion.ACEPTADA);

        // ----------------------------------------
        // ACEPTAR UNA CONVOCATORIA (CAMBIO DE ROL)
        // ----------------------------------------
        if (insc.getConvocatoria() != null) {

            ConvocatoriaEntity convocatoria = insc.getConvocatoria();

            // Actualizar club y rol
            usuarioInscrito.setClub(convocatoria.getClub());

            RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new IllegalArgumentException("Rol SOCIO no encontrado."));

            usuarioInscrito.setRol(rolSocio);

            // Cerrar sesión: incremento de versión de token
            usuarioInscrito.setTokenVersion(usuarioInscrito.getTokenVersion() + 1);

            usuarioRepository.save(usuarioInscrito);
            inscripcionRepository.save(insc);

            // Correo al usuario aceptado
            emailService.enviarCorreo(
                    usuarioInscrito.getCorreo(),
                    "Tu inscripción ha sido aceptada",
                    "<h1>Felicidades</h1><p>Has sido aceptado en la convocatoria: <b>" +
                            convocatoria.getTitulo() +
                            "</b></p>"
            );

            // --- NOTIFICACIÓN WEBSOCKET AL USUARIO (JSON CAMBIO DE ROL) ---
            notificacionService.enviarAUsuario(
                    usuarioInscrito.getId(),
                    Map.of(
                            "titulo", "¡Inscripción Aceptada!",
                            "mensaje", "Has sido aceptado en el club '" + convocatoria.getClub().getNombre() + "'. Ahora eres socio.",
                            "tipo", "CAMBIO_ROL",
                            "extraId", convocatoria.getId().toString()
                    )
            );

            // Feedback WebSocket al PRESIDENTE
            notificacionService.enviarAUsuario(
                    presidente.getId(),
                    Map.of(
                            "titulo", "Operación Exitosa",
                            "mensaje", "Nuevo socio aceptado correctamente.",
                            "tipo", "EXITO"
                    )
            );

            return;
        }

        // ----------------------------------------
        // ACEPTAR UN PROYECTO
        // ----------------------------------------
        if (insc.getProyecto() != null) {
            inscripcionRepository.save(insc);

            // --- NOTIFICACIÓN WEBSOCKET AL USUARIO (JSON STANDARD) ---
            notificacionService.enviarAUsuario(
                    usuarioInscrito.getId(),
                    Map.of(
                            "titulo", "¡Inscripción Aceptada!",
                            "mensaje", "Tu inscripción al proyecto '" + insc.getProyecto().getTitulo() + "' ha sido aceptada.",
                            "tipo", "INSCRIPCION_ACEPTADA",
                            "extraId", insc.getProyecto().getId().toString()
                    )
            );

            return;
        }

        throw new IllegalStateException("Inscripción sin referencia válida.");
    }

    // ============================================================
    // RECHAZAR INSCRIPCIÓN
    // ============================================================

    @Override
    @Transactional
    public void rechazarInscripcion(Long inscripcionId) {

        InscripcionEntity insc = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        UsuarioEntity usuarioInscrito = insc.getUsuario();

        insc.setEstado(InscripcionEntity.EstadoInscripcion.RECHAZADA);

        String tituloEvento = "";

        if (insc.getProyecto() != null) {
            ProyectoEntity proyecto = insc.getProyecto();
            tituloEvento = proyecto.getTitulo();
            proyecto.setInscritos(proyecto.getInscritos() - 1);
            proyectoRepository.save(proyecto);
        }

        if (insc.getConvocatoria() != null) {
            ConvocatoriaEntity convocatoria = insc.getConvocatoria();
            tituloEvento = convocatoria.getTitulo();
            convocatoria.setInscritos(convocatoria.getInscritos() - 1);
            convocatoriaRepository.save(convocatoria);
        }

        inscripcionRepository.save(insc);

        // Correo al usuario
        emailService.enviarCorreo(
                usuarioInscrito.getCorreo(),
                "Tu inscripción ha sido rechazada",
                "<h1>Inscripción rechazada</h1><p>Tu solicitud para <b>" + tituloEvento + "</b> ha sido rechazada.</p>"
        );

        // Notificación WebSocket al usuario rechazado
        notificacionService.enviarAUsuario(
                usuarioInscrito.getId(),
                Map.of(
                        "titulo", "Inscripción Rechazada",
                        "mensaje", "Tu inscripción a '" + tituloEvento + "' ha sido rechazada.",
                        "tipo", "ERROR" // O "RECHAZO"
                )
        );
    }

    // ============================================================
    // LISTADOS (Sin cambios)
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

    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDto> listarMisInscripcionesConvocatorias(int page, int size) {
        UsuarioEntity usuario = getUsuarioAutenticado();
        var pageEntities = inscripcionRepository.findByUsuarioIdAndConvocatoriaIsNotNull(usuario.getId(), PageRequest.of(page, size));
        return pageEntities.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDto> listarMisInscripciones(int page, int size) {
        UsuarioEntity usuario = getUsuarioAutenticado();
        var pageEntities = inscripcionRepository.findByUsuarioId(usuario.getId(), PageRequest.of(page, size));
        return pageEntities.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MisInscripcionesItemDto> obtenerMisInscripciones() {
        UsuarioEntity usuario = getUsuarioAutenticado();
        return inscripcionRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::mapToMisInscripcionesItem)
                .toList();
    }

    // ============================================================
    // MAPPERS
    // ============================================================

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
            if (c.getClub() != null) clubNombre = c.getClub().getNombre();
        } else if (insc.getProyecto() != null) {
            var p = insc.getProyecto();
            tipoEvento = "PROYECTO";
            eventoId = p.getId();
            tituloEvento = p.getTitulo();
            if (p.getClub() != null) clubNombre = p.getClub().getNombre();
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