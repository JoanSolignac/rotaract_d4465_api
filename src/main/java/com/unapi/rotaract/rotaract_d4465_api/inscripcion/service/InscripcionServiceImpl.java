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

    private final IEmailService emailService;

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
        String rolUsuario = usuario.getRol().getNombre();

        ConvocatoriaEntity convocatoria = convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Convocatoria no encontrada."));

        if (convocatoria.getCupoMaximo() - convocatoria.getInscritos() <= 0)
            throw new IllegalArgumentException("La convocatoria no tiene cupo disponible.");

        if (!rolUsuario.equalsIgnoreCase("INTERESADO"))
            throw new IllegalArgumentException("Solo usuarios INTERESADO pueden inscribirse a convocatorias.");

        boolean tieneActiva = inscripcionRepository
                .existsByUsuarioIdAndConvocatoriaIsNotNullAndEstadoIn(
                        usuario.getId(),
                        List.of(InscripcionEntity.EstadoInscripcion.PENDIENTE,
                                InscripcionEntity.EstadoInscripcion.ACEPTADA)
                );

        if (tieneActiva)
            throw new IllegalArgumentException("Ya tienes una inscripción activa en una convocatoria.");

        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .convocatoria(convocatoria)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);

        // SUMA (convocatorias)
        convocatoria.setInscritos(convocatoria.getInscritos() + 1);
        convocatoriaRepository.save(convocatoria);

        // CORREOS (igual que tu versión original)
        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Inscripción registrada - " + convocatoria.getTitulo(),
                "<h1>¡Tu inscripción fue registrada!</h1>" +
                        "<p>Convocatoria: <b>" + convocatoria.getTitulo() + "</b></p>"
        );
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

        if (!(rolUsuario.equalsIgnoreCase("SOCIO") || rolUsuario.equalsIgnoreCase("PRESIDENTE")))
            throw new IllegalArgumentException("Solo SOCIOS o PRESIDENTES pueden inscribirse.");

        if (proyecto.getCupoMaximo() - proyecto.getInscritos() <= 0)
            throw new IllegalArgumentException("El proyecto no tiene cupo disponible.");

        if (usuario.getClub() == null || proyecto.getClub() == null ||
                !usuario.getClub().getId().equals(proyecto.getClub().getId()))
            throw new IllegalArgumentException("Solo puedes inscribirte a proyectos de tu propio club.");

        if (inscripcionRepository.existsByUsuarioIdAndProyectoId(usuario.getId(), proyectoId))
            throw new IllegalArgumentException("Ya estás inscrito en este proyecto.");

        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .proyecto(proyecto)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);

        // SUMA — AHORA IGUAL QUE CONVOCATORIAS
        proyecto.setInscritos(proyecto.getInscritos() + 1);
        proyectoRepository.save(proyecto);

        // CORREOS (igual que tu versión original)
        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Inscripción registrada - " + proyecto.getTitulo(),
                "<h1>Inscripción registrada</h1>" +
                        "<p>Proyecto: <b>" + proyecto.getTitulo() + "</b></p>"
        );
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
                .orElseThrow(() -> new IllegalArgumentException("No se encontró inscripción."));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE)
            throw new IllegalStateException("Solo puedes cancelar inscripciones PENDIENTES.");

        ConvocatoriaEntity convocatoria = inscripcion.getConvocatoria();

        // RESTA
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
                .orElseThrow(() -> new IllegalArgumentException("No se encontró inscripción."));

        if (inscripcion.getEstado() != InscripcionEntity.EstadoInscripcion.PENDIENTE)
            throw new IllegalStateException("Solo puedes cancelar inscripciones PENDIENTES.");

        ProyectoEntity proyecto = inscripcion.getProyecto();

        // RESTA (igual que convocatorias)
        proyecto.setInscritos(proyecto.getInscritos() - 1);
        proyectoRepository.save(proyecto);

        inscripcionRepository.delete(inscripcion);
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

        String correoPresidente = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity presidente = usuarioRepository.findByCorreo(correoPresidente)
                .orElseThrow(() -> new IllegalArgumentException("Presidente autenticado no encontrado."));

        insc.setEstado(InscripcionEntity.EstadoInscripcion.ACEPTADA);

        // ----------------------------------------
        // CONVOCATORIA — se mantiene igual
        // ----------------------------------------
        if (insc.getConvocatoria() != null) {

            ConvocatoriaEntity convocatoria = insc.getConvocatoria();

            usuarioInscrito.setClub(convocatoria.getClub());
            RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new IllegalArgumentException("Rol SOCIO no encontrado."));
            usuarioInscrito.setRol(rolSocio);

            usuarioRepository.save(usuarioInscrito);
            inscripcionRepository.save(insc);
            return;
        }

        // ----------------------------------------
        // PROYECTO — NO SUMAR AQUÍ
        // ----------------------------------------
        if (insc.getProyecto() != null) {
            inscripcionRepository.save(insc);
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
        InscripcionEntity.EstadoInscripcion estadoAnterior = insc.getEstado();

        insc.setEstado(InscripcionEntity.EstadoInscripcion.RECHAZADA);

        // ----------------------------------------
        // RESTAR SI ERA PROYECTO (SIEMPRE)
        // ----------------------------------------
        if (insc.getProyecto() != null) {
            ProyectoEntity proyecto = insc.getProyecto();
            proyecto.setInscritos(proyecto.getInscritos() - 1);
            proyectoRepository.save(proyecto);
        }

        // ----------------------------------------
        // RESTAR SI ERA CONVOCATORIA (SIEMPRE)
        // ----------------------------------------
        if (insc.getConvocatoria() != null) {
            ConvocatoriaEntity convocatoria = insc.getConvocatoria();
            convocatoria.setInscritos(convocatoria.getInscritos() - 1);
            convocatoriaRepository.save(convocatoria);
        }

        inscripcionRepository.save(insc);
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
