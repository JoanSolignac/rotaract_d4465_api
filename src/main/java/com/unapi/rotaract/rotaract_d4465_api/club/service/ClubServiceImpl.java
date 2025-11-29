package com.unapi.rotaract.rotaract_d4465_api.club.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubCreateWithPresidenteRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.club.interfaces.IClubService;
import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.repository.AsistenciaRepository;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link IClubService} encargada de las operaciones relacionadas con clubes.
 */
@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements IClubService {

    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;

    // Repositorios requeridos para la eliminación en cascada manual
    private final ConvocatoriaRepository convocatoriaRepository;
    private final ProyectoRepository proyectoRepository;
    private final AsistenciaRepository asistenciaRepository;

    private final IEmailService emailService;
    private final com.unapi.rotaract.rotaract_d4465_api.common.services.NotificacionService notificacionService;

    // =========================================================================
    //  MÉTODOS DE LECTURA Y CREACIÓN BÁSICA
    // =========================================================================

    @Override
    public Page<ClubResponseDto> findAll(int page, int size) {
        if (size <= 0) throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        List<ClubResponseDto> clubList = clubRepository.findAll(PageRequest.of(page, size))
                .stream().filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .map(c -> ClubResponseDto.builder()
                        .id(c.getId()).nombre(c.getNombre()).departamento(c.getDepartamento())
                        .ciudad(c.getCiudad()).fechaCreacion(c.getFechaCreacion()).activo(c.getActivo()).build())
                .toList();
        return new PageImpl<>(clubList, PageRequest.of(page, size), clubList.size());
    }

    @Override
    public ClubResponseDto findById(long id) {
        ClubEntity c = clubRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Club no encontrado."));
        return ClubResponseDto.builder().id(c.getId()).nombre(c.getNombre()).departamento(c.getDepartamento())
                .ciudad(c.getCiudad()).fechaCreacion(c.getFechaCreacion()).activo(c.getActivo()).build();
    }

    @Override
    public ClubResponseDto createClub(@Valid ClubCreateRequestDto clubDto) {
        ClubEntity c = clubRepository.save(ClubEntity.builder()
                .nombre(clubDto.nombre()).departamento(clubDto.departamento()).ciudad(clubDto.ciudad())
                .fechaCreacion(LocalDate.now()).activo(true).build());
        return ClubResponseDto.builder().id(c.getId()).nombre(c.getNombre()).departamento(c.getDepartamento())
                .ciudad(c.getCiudad()).fechaCreacion(c.getFechaCreacion()).activo(c.getActivo()).build();
    }

    @Override
    public ClubResponseDto updateClub(long id, ClubEditRequestDto clubDto) {
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));

        if (!"PRESIDENTE".equals(usuarioEntity.getRol().getNombre())) {
            throw new IllegalArgumentException("No tienes permiso para actualizar un club.");
        }
        ClubEntity clubEntity = clubRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Club no encontrado."));
        if (usuarioEntity.getClub() == null || !usuarioEntity.getClub().getId().equals(clubEntity.getId())) {
            throw new IllegalArgumentException("No tienes permiso para actualizar este club.");
        }
        if (!Boolean.TRUE.equals(clubEntity.getActivo())) {
            throw new IllegalStateException("No se puede modificar un club desactivado.");
        }
        if (clubDto.nombre() != null) clubEntity.setNombre(clubDto.nombre());
        if (clubDto.departamento() != null) clubEntity.setDepartamento(clubDto.departamento());
        if (clubDto.ciudad() != null) clubEntity.setCiudad(clubDto.ciudad());

        ClubEntity updated = clubRepository.save(clubEntity);
        return ClubResponseDto.builder().id(updated.getId()).nombre(updated.getNombre())
                .departamento(updated.getDepartamento()).ciudad(updated.getCiudad())
                .fechaCreacion(updated.getFechaCreacion()).activo(updated.getActivo()).build();
    }

    // =========================================================================
    //  MÉTODOS CON LÓGICA DE NEGOCIO COMPLEJA
    // =========================================================================

    /**
     * Desactiva el club y elimina FÍSICAMENTE sus dependencias para mantener la integridad:
     * 1. Asistencias (vía Proyectos)
     * 2. Inscripciones (de Proyectos y Convocatorias)
     * 3. Proyectos
     * 4. Convocatorias
     *
     * * Solo MANTIENE a los usuarios, cambiándoles el rol a INTERESADO y desvinculándolos.
     */
    @Override
    @Transactional
    public ClubResponseDto desactivateClub(long id) {

        if (id <= 0) throw new IllegalArgumentException("El id debe ser mayor que 0.");

        ClubEntity clubEntity = clubRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
        );

        // 1. Desactivar club (Soft Delete del club en sí mismo)
        clubEntity.setActivo(false);
        ClubEntity updatedClub = clubRepository.save(clubEntity);

        // ---------------------------------------------------------------------
        // FASE 1: LIMPIEZA DE PROYECTOS (ASISTENCIAS E INSCRIPCIONES)
        // ---------------------------------------------------------------------
        // Recuperamos proyectos para limpiar sus dependencias, ya que Asistencia no tiene findByClubId
        List<ProyectoEntity> proyectos = proyectoRepository.findByClubId(id);

        for (ProyectoEntity proy : proyectos) {
            // A. Eliminar Asistencias del proyecto
            List<AsistenciaEntity> asistencias = asistenciaRepository.findByProyectoId(proy.getId());
            if (!asistencias.isEmpty()) {
                asistenciaRepository.deleteAll(asistencias);
            }

            // B. Eliminar Inscripciones del proyecto (usando método List del repo)
            List<InscripcionEntity> inscripcionesProy = inscripcionRepository.findByProyectoId(proy.getId());
            if (!inscripcionesProy.isEmpty()) {
                inscripcionRepository.deleteAll(inscripcionesProy);
            }

            // C. Eliminar el Proyecto
            proyectoRepository.delete(proy);
        }

        // ---------------------------------------------------------------------
        // FASE 2: LIMPIEZA DE CONVOCATORIAS E INSCRIPCIONES
        // ---------------------------------------------------------------------
        List<ConvocatoriaEntity> convocatorias = convocatoriaRepository.findByClubId(id);

        for (ConvocatoriaEntity conv : convocatorias) {
            // A. Eliminar Inscripciones de la convocatoria.
            // Nota: El repo solo tiene findByConvocatoriaId devolviendo Page, usamos un PageRequest grande.
            Page<InscripcionEntity> paginaInscripciones = inscripcionRepository.findByConvocatoriaId(
                    conv.getId(),
                    PageRequest.of(0, Integer.MAX_VALUE)
            );

            if (paginaInscripciones.hasContent()) {
                inscripcionRepository.deleteAll(paginaInscripciones.getContent());
            }

            // B. Eliminar la Convocatoria
            convocatoriaRepository.delete(conv);
        }

        // ---------------------------------------------------------------------
        // FASE 3: GESTIÓN DE USUARIOS (CAMBIO DE ROL Y DESVINCULACIÓN)
        // ---------------------------------------------------------------------
        List<UsuarioEntity> usuariosClub = usuarioRepository.findByClubId(updatedClub.getId());

        if (!usuariosClub.isEmpty()) {
            RolEntity rolInteresado = usuarioRepository.findRolByNombre("INTERESADO")
                    .orElseThrow(() -> new IllegalStateException("Rol INTERESADO no encontrado."));

            for (UsuarioEntity usuario : usuariosClub) {
                usuario.setRol(rolInteresado);
                usuario.setClub(null);
                incrementarTokenVersion(usuario);
                usuarioRepository.save(usuario);
            }

            // Notificar cambios masivos
            notificarDesactivacionClub(updatedClub, usuariosClub);
        }

        return ClubResponseDto.builder()
                .id(updatedClub.getId()).nombre(updatedClub.getNombre()).departamento(updatedClub.getDepartamento())
                .ciudad(updatedClub.getCiudad()).fechaCreacion(updatedClub.getFechaCreacion()).activo(updatedClub.getActivo()).build();
    }

    /**
     * Crea un club y asigna un presidente.
     */
    public ClubResponseDto createClubWithPresidente(ClubCreateWithPresidenteRequestDto dto) {
        UsuarioEntity presidente = usuarioRepository.findById(dto.presidenteId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario presidente no existe."));

        if (presidente.getClub() != null) throw new IllegalStateException("Este usuario ya pertenece a un club.");

        ClubEntity newClub = clubRepository.save(ClubEntity.builder()
                .nombre(dto.nombre()).departamento(dto.departamento()).ciudad(dto.ciudad())
                .fechaCreacion(LocalDate.now()).activo(true).build());

        presidente.setClub(newClub);
        RolEntity rolPresidente = usuarioRepository.findRolByNombre("PRESIDENTE")
                .orElseThrow(() -> new IllegalArgumentException("Rol PRESIDENTE no encontrado."));
        presidente.setRol(rolPresidente);
        incrementarTokenVersion(presidente);
        usuarioRepository.save(presidente);

        // NOTIFICACIÓN WEBSOCKET
        notificacionService.enviarAUsuario(
                presidente.getId(),
                Map.of(
                        "titulo", "¡Nuevo Club Creado!",
                        "mensaje", "Has sido asignado como Presidente del nuevo club " + newClub.getNombre() + ".",
                        "tipo", "CAMBIO_ROL",
                        "extraId", newClub.getId().toString()
                )
        );

        return ClubResponseDto.builder()
                .id(newClub.getId()).nombre(newClub.getNombre()).departamento(newClub.getDepartamento())
                .ciudad(newClub.getCiudad()).fechaCreacion(newClub.getFechaCreacion()).activo(newClub.getActivo()).build();
    }

    /**
     * Elimina un socio del club.
     */
    public void removeSocioFromClub(Long clubId, Long socioId) {
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity presidente = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        if (!"PRESIDENTE".equals(presidente.getRol().getNombre()))
            throw new IllegalArgumentException("No tienes permisos para gestionar socios.");

        ClubEntity club = clubRepository.findById(clubId).orElseThrow(() -> new IllegalArgumentException("El club no existe."));
        if (presidente.getClub() == null || !presidente.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("No puedes administrar un club que no diriges.");

        UsuarioEntity socio = usuarioRepository.findById(socioId).orElseThrow(() -> new IllegalArgumentException("El socio no existe."));
        if (socio.getClub() == null || !socio.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("Ese usuario no pertenece a tu club.");
        if ("PRESIDENTE".equals(socio.getRol().getNombre()))
            throw new IllegalArgumentException("No puedes eliminar al presidente del club.");

        // Cancelar inscripciones activas (PENDIENTE/ACEPTADA) y eliminar para limpieza
        List<InscripcionEntity> inscripciones = inscripcionRepository.findByUsuarioId(socio.getId()).stream()
                .filter(i -> i.getConvocatoria() != null && (i.getEstado() == InscripcionEntity.EstadoInscripcion.ACEPTADA || i.getEstado() == InscripcionEntity.EstadoInscripcion.PENDIENTE))
                .toList();

        // Aquí solo cancelamos porque el socio se va, pero la convocatoria sigue existiendo para otros
        for (InscripcionEntity ins : inscripciones) {
            ins.setEstado(InscripcionEntity.EstadoInscripcion.CANCELADA);
            var conv = ins.getConvocatoria();
            if (conv != null) conv.setInscritos(conv.getInscritos() - 1);
        }
        inscripcionRepository.saveAll(inscripciones);

        // Cambiar rol a INTERESADO
        RolEntity rolInteresado = usuarioRepository.findRolByNombre("INTERESADO")
                .orElseThrow(() -> new IllegalStateException("Rol INTERESADO no encontrado."));
        socio.setRol(rolInteresado);
        socio.setClub(null);
        incrementarTokenVersion(socio);
        usuarioRepository.save(socio);

        notificarRemocionSocio(socio, club, presidente);
    }

    /**
     * Transfiere la presidencia.
     */
    public void transferirPresidencia(Long clubId, Long nuevoPresidenteId) {
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity actualPresidente = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        if (!"PRESIDENTE".equals(actualPresidente.getRol().getNombre()))
            throw new IllegalArgumentException("No permiso.");
        if (actualPresidente.getClub() == null || !actualPresidente.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("No perteneces a este club.");

        ClubEntity club = clubRepository.findById(clubId).orElseThrow(() -> new IllegalArgumentException("Club no encontrado."));
        UsuarioEntity nuevoPresidente = usuarioRepository.findById(nuevoPresidenteId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (nuevoPresidente.getClub() == null || !nuevoPresidente.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("El usuario no pertenece al club.");

        RolEntity rolPresidente = usuarioRepository.findRolByNombre("PRESIDENTE").orElseThrow();
        RolEntity rolSocio = usuarioRepository.findRolByNombre("SOCIO").orElseThrow();

        actualPresidente.setRol(rolSocio);
        nuevoPresidente.setRol(rolPresidente);

        incrementarTokenVersion(actualPresidente);
        incrementarTokenVersion(nuevoPresidente);

        usuarioRepository.save(actualPresidente);
        usuarioRepository.save(nuevoPresidente);

        notificarTransferenciaPresidencia(actualPresidente, nuevoPresidente, club);
    }

    private void incrementarTokenVersion(UsuarioEntity usuario) {
        Integer versionActual = usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion();
        usuario.setTokenVersion(versionActual + 1);
    }

    // ============================================================
    // MÉTODOS DE NOTIFICACIÓN
    // ============================================================

    private void notificarDesactivacionClub(ClubEntity club, List<UsuarioEntity> usuariosClub) {
        String asunto = "Club desactivado - Rotaract D4465";
        for (UsuarioEntity usuario : usuariosClub) {
            emailService.enviarCorreo(usuario.getCorreo(), asunto,
                    "<h2>Notificación de desactivación</h2><p>El club <strong>" + club.getNombre() +
                            "</strong> ha sido desactivado. Su rol ahora es INTERESADO.</p>");

            notificacionService.enviarAUsuario(
                    usuario.getId(),
                    Map.of("titulo", "Club desactivado", "mensaje", "El club " + club.getNombre() + " ha sido desactivado.", "tipo", "CAMBIO_ROL")
            );
        }
    }

    private void notificarRemocionSocio(UsuarioEntity socio, ClubEntity club, UsuarioEntity presidente) {
        emailService.enviarCorreo(socio.getCorreo(), "Remoción de club - Rotaract D4465",
                "Ha sido removido del club " + club.getNombre() + " por " + presidente.getNombre());
        notificacionService.enviarAUsuario(
                socio.getId(),
                Map.of("titulo", "Remoción de club", "mensaje", "Has sido removido del club. Tu rol ahora es INTERESADO.", "tipo", "CAMBIO_ROL", "extraId", club.getId().toString())
        );
    }

    private void notificarTransferenciaPresidencia(UsuarioEntity anterior, UsuarioEntity nuevo, ClubEntity club) {
        emailService.enviarCorreo(nuevo.getCorreo(), "Designado Presidente", "Es el nuevo presidente de " + club.getNombre());
        emailService.enviarCorreo(anterior.getCorreo(), "Transferencia confirmada", "Ha transferido la presidencia de " + club.getNombre());
        notificacionService.enviarAUsuario(nuevo.getId(), Map.of("titulo", "Nueva presidencia", "mensaje", "Eres presidente de " + club.getNombre(), "tipo", "CAMBIO_ROL", "extraId", club.getId().toString()));
        notificacionService.enviarAUsuario(anterior.getId(), Map.of("titulo", "Presidencia transferida", "mensaje", "Transferiste la presidencia a " + nuevo.getNombre(), "tipo", "CAMBIO_ROL", "extraId", club.getId().toString()));
    }
}