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
    private final IEmailService emailService;
    private final com.unapi.rotaract.rotaract_d4465_api.common.services.NotificacionService notificacionService;

    // ... (Métodos de búsqueda findAll, findById, createClub y updateClub se mantienen igual) ...
    // Para ahorrar espacio en la respuesta, omito los métodos de lectura/creación básica que no envían notificaciones
    // y me centro en los que cambiamos la lógica de notificación.

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
    //  MÉTODOS CON LÓGICA DE NOTIFICACIÓN ESTANDARIZADA
    // =========================================================================

    /**
     * Desactiva (marca como inactivo) el club identificado por {@code id}.
     */
    @Override
    @Transactional
    public ClubResponseDto desactivateClub(long id) {

        if (id <= 0) throw new IllegalArgumentException("El id debe ser mayor que 0.");

        ClubEntity clubEntity = clubRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
        );

        // 1. Desactivar club
        clubEntity.setActivo(false);
        ClubEntity updatedClub = clubRepository.save(clubEntity);

        // 2. Obtener todos los usuarios del club
        List<UsuarioEntity> usuariosClub = usuarioRepository.findByClubId(updatedClub.getId());

        if (!usuariosClub.isEmpty()) {
            RolEntity rolInteresado = usuarioRepository.findRolByNombre("INTERESADO")
                    .orElseThrow(() -> new IllegalStateException("Rol INTERESADO no encontrado."));

            for (UsuarioEntity usuario : usuariosClub) {
                // Cancelar inscripciones activas
                List<InscripcionEntity> inscripciones = inscripcionRepository
                        .findByUsuarioId(usuario.getId())
                        .stream()
                        .filter(i -> i.getConvocatoria() != null)
                        .filter(i -> i.getEstado() == InscripcionEntity.EstadoInscripcion.ACEPTADA
                                || i.getEstado() == InscripcionEntity.EstadoInscripcion.PENDIENTE)
                        .toList();

                for (InscripcionEntity ins : inscripciones) {
                    ins.setEstado(InscripcionEntity.EstadoInscripcion.CANCELADA);
                    var conv = ins.getConvocatoria();
                    if (conv != null) conv.setInscritos(conv.getInscritos() - 1);
                }
                inscripcionRepository.saveAll(inscripciones);

                // Cambiar rol a INTERESADO
                usuario.setRol(rolInteresado);
                usuario.setClub(null);
                incrementarTokenVersion(usuario);
                usuarioRepository.save(usuario);
            }

            // Notificar estandarizado
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

        // NOTIFICACIÓN WEBSOCKET AL NUEVO PRESIDENTE
        notificacionService.enviarAUsuario(
                presidente.getId(),
                Map.of(
                        "titulo", "¡Nuevo Club Creado!",
                        "mensaje", "Has sido asignado como Presidente del nuevo club " + newClub.getNombre() + ".",
                        "tipo", "CAMBIO_ROL", // Importante para que el frontend actualice permisos
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

        // Cancelar inscripciones
        List<InscripcionEntity> inscripciones = inscripcionRepository.findByUsuarioId(socio.getId()).stream()
                .filter(i -> i.getConvocatoria() != null && (i.getEstado() == InscripcionEntity.EstadoInscripcion.ACEPTADA || i.getEstado() == InscripcionEntity.EstadoInscripcion.PENDIENTE))
                .toList();
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
    // MÉTODOS DE NOTIFICACIÓN ACTUALIZADOS (USAN MAP Y TIPO)
    // ============================================================

    private void notificarDesactivacionClub(ClubEntity club, List<UsuarioEntity> usuariosClub) {
        String asunto = "Club desactivado - Rotaract D4465";
        for (UsuarioEntity usuario : usuariosClub) {
            String mensajeCorreo = """
                    <h2>Notificación de desactivación de club</h2>
                    <p>El club <strong>%s</strong> ha sido desactivado.</p>
                    <p>Su rol ha sido actualizado a <strong>INTERESADO</strong>.</p>
                    """.formatted(club.getNombre());

            emailService.enviarCorreo(usuario.getCorreo(), asunto, mensajeCorreo);

            // WebSocket Estandarizado
            notificacionService.enviarAUsuario(
                    usuario.getId(),
                    Map.of(
                            "titulo", "Club desactivado",
                            "mensaje", "El club " + club.getNombre() + " ha sido desactivado. Ahora eres INTERESADO.",
                            "tipo", "CAMBIO_ROL" // IMPORTANTE
                    )
            );
        }
    }

    private void notificarRemocionSocio(UsuarioEntity socio, ClubEntity club, UsuarioEntity presidente) {
        String asunto = "Ha sido removido del club - Rotaract D4465";
        String html = """
                <h2>Remoción de club</h2>
                <p>Ha sido removido del club <strong>%s</strong> por %s.</p>
                <p>Su rol ha sido actualizado a <strong>INTERESADO</strong>.</p>
                """.formatted(club.getNombre(), presidente.getNombre());

        emailService.enviarCorreo(socio.getCorreo(), asunto, html);

        // WebSocket Estandarizado
        notificacionService.enviarAUsuario(
                socio.getId(),
                Map.of(
                        "titulo", "Remoción de club",
                        "mensaje", "Has sido removido del club " + club.getNombre() + ". Tu rol ahora es INTERESADO.",
                        "tipo", "CAMBIO_ROL", // IMPORTANTE
                        "extraId", club.getId().toString()
                )
        );
    }

    private void notificarTransferenciaPresidencia(UsuarioEntity anterior, UsuarioEntity nuevo, ClubEntity club) {
        // Correo nuevo presidente
        emailService.enviarCorreo(nuevo.getCorreo(), "Designado Presidente - Rotaract D4465",
                "Ha sido designado como Presidente del club " + club.getNombre());
        // Correo expresidente
        emailService.enviarCorreo(anterior.getCorreo(), "Transferencia confirmada - Rotaract D4465",
                "Ha transferido la presidencia del club " + club.getNombre());

        // WebSocket Nuevo Presidente
        notificacionService.enviarAUsuario(
                nuevo.getId(),
                Map.of(
                        "titulo", "Nueva presidencia asignada",
                        "mensaje", "Ahora eres presidente del club " + club.getNombre() + ".",
                        "tipo", "CAMBIO_ROL",
                        "extraId", club.getId().toString()
                )
        );

        // WebSocket Anterior Presidente
        notificacionService.enviarAUsuario(
                anterior.getId(),
                Map.of(
                        "titulo", "Presidencia transferida",
                        "mensaje", "Has transferido la presidencia a " + nuevo.getNombre() + ". Ahora eres SOCIO.",
                        "tipo", "CAMBIO_ROL",
                        "extraId", club.getId().toString()
                )
        );
    }
}