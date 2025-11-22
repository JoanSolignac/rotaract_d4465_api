package com.unapi.rotaract.rotaract_d4465_api.inscripcion.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
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
 * Servicio que gestiona inscripciones a convocatorias y proyectos.
 */
@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements IInscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final ProyectoRepository proyectoRepository;

    // INYECCIÓN DE CORREO
    private final IEmailService emailService;

    /**
     * Obtiene el usuario autenticado desde el SecurityContext.
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

        // Incrementar inscritos (contador de postulaciones)
        convocatoria.setInscritos(convocatoria.getInscritos() + 1);
        convocatoriaRepository.save(convocatoria);

        // ============================================================
        // ENVÍO DE CORREOS
        // ============================================================

        // 1. Correo al usuario interesado
        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Inscripción registrada - " + convocatoria.getTitulo(),
                "<h1>¡Tu inscripción fue registrada!</h1>" +
                        "<p>Has solicitado participar en la convocatoria:</p>" +
                        "<p><b>" + convocatoria.getTitulo() + "</b></p>" +
                        "<p>Club organizador: <b>" + convocatoria.getClub().getNombre() + "</b></p>" +
                        "<p>Un presidente del club revisará tu solicitud.</p>"
        );

        // 2. Correo al presidente del club
        if (convocatoria.getClub() != null && convocatoria.getClub().getMiembros() != null) {
            convocatoria.getClub().getMiembros().stream()
                    .filter(miembro -> miembro.getRol() != null
                            && miembro.getRol().getNombre().equalsIgnoreCase("PRESIDENTE"))
                    .findFirst()
                    .ifPresent(presidente ->
                            emailService.enviarCorreo(
                                    presidente.getCorreo(),
                                    "Nuevo inscrito en tu convocatoria",
                                    "<h1>Nueva inscripción recibida</h1>" +
                                            "<p>El usuario <b>" + usuario.getNombre() + "</b> (" + usuario.getCorreo() + ")</p>" +
                                            "<p>Se ha inscrito a la convocatoria:</p>" +
                                            "<p><b>" + convocatoria.getTitulo() + "</b></p>"
                            )
                    );
        }

    }

    // ============================================================
    // INSCRIPCIÓN EN PROYECTOS (SOCIO / PRESIDENTE)
    // ============================================================

    @Override
    @Transactional
    public void inscribirseEnProyecto(Long proyectoId) {

        UsuarioEntity usuario = getUsuarioAutenticado();
        String rolUsuario = usuario.getRol().getNombre();

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        if (!(rolUsuario.equalsIgnoreCase("SOCIO") || rolUsuario.equalsIgnoreCase("PRESIDENTE"))) {
            throw new IllegalArgumentException("Solo SOCIOS o PRESIDENTES pueden inscribirse a proyectos.");
        }

        if (proyecto.getCupoMaximo() - proyecto.getInscritos() <= 0) {
            throw new IllegalArgumentException("El proyecto no tiene cupo disponible.");
        }

        // Validar que el usuario pertenezca al mismo club del proyecto
        if (usuario.getClub() == null || proyecto.getClub() == null ||
                !usuario.getClub().getId().equals(proyecto.getClub().getId())) {
            throw new IllegalArgumentException("Solo puedes inscribirte a proyectos de tu propio club.");
        }

        if (inscripcionRepository.existsByUsuarioIdAndProyectoId(usuario.getId(), proyectoId)) {
            throw new IllegalArgumentException("Ya estás inscrito en este proyecto.");
        }

        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .proyecto(proyecto)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);

        // ============================================================
        // ENVÍO DE CORREOS
        // ============================================================

        // 1. Correo al socio/presidente que se inscribe
        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Inscripción registrada - Proyecto: " + proyecto.getTitulo(),
                "<h1>Inscripción registrada</h1>" +
                        "<p>Te has inscrito al proyecto:</p>" +
                        "<p><b>" + proyecto.getTitulo() + "</b></p>" +
                        (proyecto.getClub() != null
                                ? "<p>Club responsable: <b>" + proyecto.getClub().getNombre() + "</b></p>"
                                : "") +
                        "<p>Tu inscripción está en estado <b>PENDIENTE</b> hasta que el presidente la apruebe.</p>"
        );

        // 2. Correo al presidente del club del proyecto
        if (proyecto.getClub() != null && proyecto.getClub().getMiembros() != null) {
            proyecto.getClub().getMiembros().stream()
                    .filter(miembro -> miembro.getRol() != null
                            && miembro.getRol().getNombre().equalsIgnoreCase("PRESIDENTE"))
                    .findFirst()
                    .ifPresent(presidente ->
                            emailService.enviarCorreo(
                                    presidente.getCorreo(),
                                    "Nuevo inscrito en tu proyecto",
                                    "<h1>Nueva inscripción a proyecto</h1>" +
                                            "<p>El usuario <b>" + usuario.getNombre() + "</b> (" + usuario.getCorreo() + ")</p>" +
                                            "<p>Se ha inscrito al proyecto:</p>" +
                                            "<p><b>" + proyecto.getTitulo() + "</b></p>"
                            )
                    );
        }
    }

    // ============================================================
    // CANCELAR INSCRIPCIÓN (CONVOCATORIA)
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
                .orElseThrow(() -> new IllegalArgumentException("No se encontró inscripción."));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar inscripciones PENDIENTES.");
        }

        ConvocatoriaEntity convocatoria = inscripcion.getConvocatoria();
        convocatoria.setInscritos(convocatoria.getInscritos() - 1);
        convocatoriaRepository.save(convocatoria);

        inscripcionRepository.delete(inscripcion);

        // Correo al usuario confirmando cancelación
        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Inscripción cancelada - " + convocatoria.getTitulo(),
                "<h1>Inscripción cancelada</h1>" +
                        "<p>Has cancelado tu inscripción a la convocatoria:</p>" +
                        "<p><b>" + convocatoria.getTitulo() + "</b></p>"
        );
    }

    // ============================================================
    // CANCELAR INSCRIPCIÓN (PROYECTO)
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
                .orElseThrow(() -> new IllegalArgumentException("No se encontró inscripción."));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar inscripciones PENDIENTES.");
        }

        ProyectoEntity proyecto = inscripcion.getProyecto();

        inscripcionRepository.delete(inscripcion);

        // Correo al usuario confirmando cancelación
        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Inscripción cancelada - Proyecto: " + proyecto.getTitulo(),
                "<h1>Inscripción cancelada</h1>" +
                        "<p>Has cancelado tu inscripción al proyecto:</p>" +
                        "<p><b>" + proyecto.getTitulo() + "</b></p>"
        );
    }

    // ============================================================
    // ACEPTAR INSCRIPCIÓN
    // ============================================================

    @Override
    @Transactional
    public void aceptarInscripcion(Long inscripcionId) {

        InscripcionEntity insc = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        UsuarioEntity usuarioInscrito = insc.getUsuario();

        // Presidente que acepta (usuario autenticado)
        String correoPresidente = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity presidente = usuarioRepository.findByCorreo(correoPresidente)
                .orElseThrow(() -> new IllegalArgumentException("Presidente autenticado no encontrado."));

        insc.setEstado(InscripcionEntity.EstadoInscripcion.ACEPTADA);

        // --------------------------------------------------------
        // CASO: CONVOCATORIA
        // --------------------------------------------------------
        if (insc.getConvocatoria() != null) {

            ConvocatoriaEntity convocatoria = insc.getConvocatoria();

            // Asignar club al usuario y cambiar a SOCIO
            usuarioInscrito.setClub(convocatoria.getClub());

            RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new IllegalArgumentException("Rol SOCIO no encontrado."));
            usuarioInscrito.setRol(rolSocio);

            usuarioRepository.save(usuarioInscrito);
            inscripcionRepository.save(insc);

            // Correo al usuario aceptado
            emailService.enviarCorreo(
                    usuarioInscrito.getCorreo(),
                    "Has sido aceptado en la convocatoria: " + convocatoria.getTitulo(),
                    "<h1>¡Felicidades, " + usuarioInscrito.getNombre() + "!</h1>" +
                            "<p>Has sido <b>aceptado</b> en la convocatoria:</p>" +
                            "<h2>" + convocatoria.getTitulo() + "</h2>" +
                            "<p>Ahora eres parte del club <b>" + convocatoria.getClub().getNombre() + "</b>.</p>" +
                            "<p>Pronto recibirás más indicaciones de tu club.</p>"
            );

            // Correo al presidente que aceptó
            emailService.enviarCorreo(
                    presidente.getCorreo(),
                    "Aceptaste una inscripción en tu convocatoria",
                    "<h1>Confirmación de aceptación</h1>" +
                            "<p>Has aceptado a:</p>" +
                            "<p><b>" + usuarioInscrito.getNombre() + "</b> (" + usuarioInscrito.getCorreo() + ")</p>" +
                            "<p>En la convocatoria:</p>" +
                            "<p><b>" + convocatoria.getTitulo() + "</b></p>"
            );

            return;
        }

        // --------------------------------------------------------
        // CASO: PROYECTO
        // --------------------------------------------------------
        if (insc.getProyecto() != null) {

            ProyectoEntity proyecto = insc.getProyecto();
            proyecto.setInscritos(proyecto.getInscritos() + 1);

            proyectoRepository.save(proyecto);
            inscripcionRepository.save(insc);

            // Correo al usuario aceptado
            emailService.enviarCorreo(
                    usuarioInscrito.getCorreo(),
                    "Has sido aceptado en el proyecto: " + proyecto.getTitulo(),
                    "<h1>Has sido aceptado en el proyecto</h1>" +
                            "<p>Proyecto:</p>" +
                            "<h2>" + proyecto.getTitulo() + "</h2>" +
                            (proyecto.getClub() != null
                                    ? "<p>Club responsable: <b>" + proyecto.getClub().getNombre() + "</b></p>"
                                    : "") +
                            "<p>Te esperamos en las fechas indicadas del proyecto.</p>"
            );

            // Correo al presidente que aceptó
            emailService.enviarCorreo(
                    presidente.getCorreo(),
                    "Aceptaste una inscripción en tu proyecto",
                    "<h1>Confirmación de aceptación</h1>" +
                            "<p>Has aceptado a:</p>" +
                            "<p><b>" + usuarioInscrito.getNombre() + "</b> (" + usuarioInscrito.getCorreo() + ")</p>" +
                            "<p>En el proyecto:</p>" +
                            "<p><b>" + proyecto.getTitulo() + "</b></p>"
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

        // Guardar estado previo por si se requiere lógica futura
        InscripcionEntity.EstadoInscripcion estadoAnterior = insc.getEstado();

        insc.setEstado(InscripcionEntity.EstadoInscripcion.RECHAZADA);

        // Si pertenecía a proyecto y estaba previamente aceptada, decrementar inscritos
        if (insc.getProyecto() != null && estadoAnterior == InscripcionEntity.EstadoInscripcion.ACEPTADA) {
            ProyectoEntity proyecto = insc.getProyecto();
            proyecto.setInscritos(proyecto.getInscritos() - 1);
            proyectoRepository.save(proyecto);
        }

        inscripcionRepository.save(insc);

        // Correo al usuario notificando rechazo
        String asunto;
        String cuerpoHtml;

        if (insc.getConvocatoria() != null) {
            ConvocatoriaEntity convocatoria = insc.getConvocatoria();
            asunto = "Tu inscripción fue rechazada - " + convocatoria.getTitulo();
            cuerpoHtml =
                    "<h1>Tu inscripción fue rechazada</h1>" +
                            "<p>Lamentablemente, tu inscripción a la convocatoria:</p>" +
                            "<p><b>" + convocatoria.getTitulo() + "</b></p>" +
                            "<p>ha sido <b>rechazada</b>.</p>";
        } else if (insc.getProyecto() != null) {
            ProyectoEntity proyecto = insc.getProyecto();
            asunto = "Tu inscripción fue rechazada - Proyecto: " + proyecto.getTitulo();
            cuerpoHtml =
                    "<h1>Tu inscripción fue rechazada</h1>" +
                            "<p>Lamentablemente, tu inscripción al proyecto:</p>" +
                            "<p><b>" + proyecto.getTitulo() + "</b></p>" +
                            "<p>ha sido <b>rechazada</b>.</p>";
        } else {
            asunto = "Tu inscripción fue rechazada";
            cuerpoHtml =
                    "<h1>Tu inscripción fue rechazada</h1>" +
                            "<p>La inscripción asociada a tu cuenta ha sido rechazada.</p>";
        }

        emailService.enviarCorreo(
                usuarioInscrito.getCorreo(),
                asunto,
                cuerpoHtml
        );
    }

    // ============================================================
    // LISTADOS
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
