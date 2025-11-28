package com.unapi.rotaract.rotaract_d4465_api.proyecto.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.NotificacionDto;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
import com.unapi.rotaract.rotaract_d4465_api.common.services.NotificacionService;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.interfaces.IProyectoService;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio encargado de gestionar la información y el ciclo de vida
 * de los proyectos del sistema. Incluye operaciones de consulta,
 * creación, actualización, cancelación, finalización y búsqueda por filtros.
 */
@Service
@RequiredArgsConstructor
public class ProyectoServiceImpl implements IProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final IEmailService emailService;
    private final NotificacionService notificacionService;

    // ============================================================
    // LISTAR TODOS LOS PROYECTOS
    // ============================================================

    @Override
    public Page<ProyectoResponseDto> findAll(int page, int size) {
        return proyectoRepository.findAll(PageRequest.of(page, size))
                .map(p -> mapToResponse(p, false));
    }

    // ============================================================
    // LISTAR PROYECTOS DEL CLUB DEL USUARIO (SOCIO / PRESIDENTE)
    // ============================================================

    @Override
    public Page<ProyectoResponseDto> findAllBySocioPresidente(int page, int size) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        List<ProyectoResponseDto> proyectos = proyectoRepository.findByClubId(usuario.getClub().getId())
                .stream()
                .map(p -> mapToResponse(p, false))
                .toList();

        return new PageImpl<>(
                proyectos,
                PageRequest.of(page, size),
                proyectos.size()
        );
    }

    // ============================================================
    // LISTAR PROYECTOS DISPONIBLES PARA EL USUARIO (NO INSCRITOS)
    // ============================================================

    @Override
    public Page<ProyectoResponseDto> findDisponiblesParaUsuario(int page, int size) {

        UsuarioEntity usuario = getUsuarioAutenticado();
        Long clubId = usuario.getClub().getId();

        Page<ProyectoEntity> proyectosClub =
                new PageImpl<>(
                        proyectoRepository.findByClubId(clubId),
                        PageRequest.of(page, size),
                        proyectoRepository.findByClubId(clubId).size()
                );

        List<ProyectoResponseDto> disponibles = proyectosClub.stream()
                .filter(p -> !inscripcionRepository.existsByUsuarioIdAndProyectoId(usuario.getId(), p.getId()))
                .map(p -> mapToResponse(p, true))
                .toList();

        return new PageImpl<>(
                disponibles,
                PageRequest.of(page, size),
                disponibles.size()
        );
    }

    // ============================================================
    // BUSCAR POR ID
    // ============================================================

    @Override
    public ProyectoResponseDto findById(Long id) {
        ProyectoEntity entity = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));
        return mapToResponse(entity, false);
    }

    // ============================================================
    // CREAR PROYECTO
    // ============================================================

    @Override
    @Transactional
    public ProyectoResponseDto create(ProyectoCreateRequestDto dto) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        if (usuario.getClub() == null)
            throw new RuntimeException("El usuario no tiene un club asignado.");

        ProyectoEntity proyecto = new ProyectoEntity();

        proyecto.setTitulo(dto.titulo());
        proyecto.setDescripcion(dto.descripcion());
        proyecto.setLugar(dto.lugar());
        proyecto.setRequisitos(dto.requisitos());
        proyecto.setFechaPublicacion(dto.fechaInicioPostulacion());
        proyecto.setFechaCierre(dto.fechaFinPostulacion());
        proyecto.setEstado(EventoEntity.EstadoEvento.ACTIVO);
        proyecto.setClub(usuario.getClub());
        proyecto.setCupoMaximo(dto.cupoMaximo());
        proyecto.setInscritos(0);
        proyecto.setObjetivo(dto.objetivo());
        proyecto.setFechaInicioPostulacion(dto.fechaInicioPostulacion());
        proyecto.setFechaFinPostulacion(dto.fechaFinPostulacion());
        proyecto.setFechaInicioProyecto(dto.fechaInicioProyecto());
        proyecto.setFechaFinProyecto(dto.fechaFinProyecto());
        proyecto.setEstadoProyecto(ProyectoEntity.EstadoProyecto.EN_POSTULACION);

        proyecto.setAsistenciaActiva(false);
        proyecto.setAsistenciaCerrada(false);

        proyectoRepository.save(proyecto);

        // ========================================================
        // NOTIFICAR POR CORREO A TODOS LOS SOCIOS DEL CLUB
        // ========================================================

        List<UsuarioEntity> socios = usuarioRepository.findByClubId(usuario.getClub().getId());

        for (UsuarioEntity socio : socios) {

            String asunto = "Nuevo proyecto creado en tu club";

            String html = """
                    <h2>Nuevo proyecto creado</h2>
                    <p>Se ha creado el proyecto <strong>%s</strong> en tu club.</p>
                    <p>Descripción: %s</p>
                    """.formatted(
                    proyecto.getTitulo(),
                    proyecto.getDescripcion()
            );

            emailService.enviarCorreo(socio.getCorreo(), asunto, html);

            notificacionService.enviarAUsuario(
                    socio.getId(),
                    new NotificacionDto(
                            "Nuevo proyecto",
                            "Se creó el proyecto: " + proyecto.getTitulo()
                    )
            );
        }

        return mapToResponse(proyecto, false);
    }

    // ============================================================
    // EDITAR PROYECTO
    // ============================================================

    @Override
    @Transactional
    public ProyectoResponseDto update(Long id, ProyectoEditRequestDto dto) {

        ProyectoEntity p = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));

        if (dto.titulo() != null) p.setTitulo(dto.titulo());
        if (dto.descripcion() != null) p.setDescripcion(dto.descripcion());
        if (dto.requisitos() != null) p.setRequisitos(dto.requisitos());
        if (dto.lugar() != null) p.setLugar(dto.lugar());
        if (dto.cupoMaximo() != null) p.setCupoMaximo(dto.cupoMaximo());
        if (dto.objetivo() != null) p.setObjetivo(dto.objetivo());
        if (dto.fechaInicioPostulacion() != null) p.setFechaInicioPostulacion(dto.fechaInicioPostulacion());
        if (dto.fechaFinPostulacion() != null) p.setFechaFinPostulacion(dto.fechaFinPostulacion());
        if (dto.fechaInicioProyecto() != null) p.setFechaInicioProyecto(dto.fechaInicioProyecto());
        if (dto.fechaFinProyecto() != null) p.setFechaFinProyecto(dto.fechaFinProyecto());

        proyectoRepository.save(p);

        // ========================================================
        // NOTIFICAR EDICIÓN POR CORREO Y WEBSOCKET
        // ========================================================

        List<UsuarioEntity> socios = usuarioRepository.findByClubId(p.getClub().getId());

        for (UsuarioEntity socio : socios) {

            String asunto = "Actualización de proyecto";

            String html = """
                    <h2>Proyecto actualizado</h2>
                    <p>El proyecto <strong>%s</strong> ha sido actualizado.</p>
                    """.formatted(p.getTitulo());

            emailService.enviarCorreo(socio.getCorreo(), asunto, html);

            notificacionService.enviarAUsuario(
                    socio.getId(),
                    new NotificacionDto(
                            "Proyecto actualizado",
                            "El proyecto " + p.getTitulo() + " ha sido modificado."
                    )
            );
        }

        return mapToResponse(p, false);
    }

    // ============================================================
    // CANCELAR PROYECTO
    // ============================================================

    @Override
    @Transactional
    public void cancelarProyecto(Long id) {

        ProyectoEntity p = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));

        // **VALIDACIÓN CLAVE**
        if (LocalDate.now().isAfter(p.getFechaInicioProyecto())) {
            throw new RuntimeException("No se puede cancelar un proyecto que ya está en ejecución.");
        }

        p.setEstado(EventoEntity.EstadoEvento.CANCELADO);
        p.setEstadoProyecto(ProyectoEntity.EstadoProyecto.CANCELADO);
        p.cerrarAsistencia();

        proyectoRepository.save(p);

        // ========================================================
        // NOTIFICAR CANCELACIÓN
        // ========================================================

        List<UsuarioEntity> socios = usuarioRepository.findByClubId(p.getClub().getId());

        for (UsuarioEntity socio : socios) {

            String asunto = "Proyecto cancelado";

            String html = """
                    <h2>Proyecto cancelado</h2>
                    <p>El proyecto <strong>%s</strong> ha sido cancelado.</p>
                    """.formatted(p.getTitulo());

            emailService.enviarCorreo(socio.getCorreo(), asunto, html);

            notificacionService.enviarAUsuario(
                    socio.getId(),
                    new NotificacionDto(
                            "Proyecto cancelado",
                            "El proyecto " + p.getTitulo() + " ha sido cancelado."
                    )
            );
        }
    }

    // ============================================================
    // FINALIZAR PROYECTO
    // ============================================================

    @Override
    @Transactional
    public void finalizarProyecto(Long id) {

        ProyectoEntity p = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));

        p.setEstado(EventoEntity.EstadoEvento.CERRADO);
        p.setEstadoProyecto(ProyectoEntity.EstadoProyecto.FINALIZADO);
        p.cerrarAsistencia();

        proyectoRepository.save(p);

        List<UsuarioEntity> socios = usuarioRepository.findByClubId(p.getClub().getId());

        for (UsuarioEntity socio : socios) {
            notificacionService.enviarAUsuario(
                    socio.getId(),
                    new NotificacionDto(
                            "Proyecto finalizado",
                            "El proyecto " + p.getTitulo() + " ha finalizado."
                    )
            );
        }
    }

    // ============================================================
    // BUSCAR POR TÍTULO
    // ============================================================

    @Override
    public Page<ProyectoResponseDto> buscarPorTitulo(String titulo, int page, int size) {
        return proyectoRepository
                .findByTituloContainingIgnoreCase(titulo, PageRequest.of(page, size))
                .map(p -> mapToResponse(p, false));
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private ProyectoResponseDto mapToResponse(ProyectoEntity e, boolean disponible) {

        return ProyectoResponseDto
                .builder()
                .id(e.getId())
                .estadoProyecto(e.getEstadoProyecto().toString())
                .cupoMaximo(e.getCupoMaximo())
                .titulo(e.getTitulo())
                .descripcion(e.getDescripcion())
                .objetivo(e.getObjetivo())
                .requisitos(e.getRequisitos())
                .lugar(e.getLugar())
                .fechaInicioPostulacion(e.getFechaInicioPostulacion())
                .fechaFinPostulacion(e.getFechaFinPostulacion())
                .fechaInicioProyecto(e.getFechaInicioProyecto())
                .fechaFinProyecto(e.getFechaFinProyecto())
                .clubId(e.getClub() != null ? e.getClub().getId() : null)
                .clubNombre(e.getClub() != null ? e.getClub().getNombre() : null)
                .inscritos(e.getInscritos())
                .disponible(disponible)
                .build();
    }

    // ============================================================
    // UTILIDAD
    // ============================================================

    private UsuarioEntity getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado."));
    }
}
