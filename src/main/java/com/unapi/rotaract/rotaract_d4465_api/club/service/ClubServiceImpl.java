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
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.NotificacionDto;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación de {@link IClubService} encargada de las operaciones relacionadas con clubes.
 * Esta clase consulta y delega en {@link ClubRepository} para recuperar datos de clubes y expone operaciones con soporte de paginación.
 * Convenciones y comportamiento esperado:
 * - El parámetro "page" se interpreta como índice 0-based (0 = primera página).
 * - El parámetro "size" representa el número máximo de elementos por página y debe ser > 0.
 * - Si no hay resultados para la página solicitada, se devuelve una {@link Page} vacía en lugar de null.
 */
@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements IClubService {

    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final IEmailService emailService;
    private final com.unapi.rotaract.rotaract_d4465_api.common.services.NotificacionService notificacionService;

    /**
     * Recupera una página de clubes.
     */
    @Override
    public Page<ClubResponseDto> findAll(int page, int size) {

        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        }

        List<ClubResponseDto> clubList = clubRepository.findAll(PageRequest.of(page, size))
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .map(
                        clubEntity -> ClubResponseDto.builder()
                                .id(clubEntity.getId())
                                .nombre(clubEntity.getNombre())
                                .departamento(clubEntity.getDepartamento())
                                .ciudad(clubEntity.getCiudad())
                                .fechaCreacion(clubEntity.getFechaCreacion())
                                .activo(clubEntity.getActivo())
                                .build()
                ).toList();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(clubList, pageable, clubList.size());
    }

    /**
     * Recupera un club por su identificador.
     */
    @Override
    public ClubResponseDto findById(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que 0.");
        }

        ClubEntity clubEntity = clubRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
                );

        return ClubResponseDto
                .builder()
                .id(clubEntity.getId())
                .nombre(clubEntity.getNombre())
                .departamento(clubEntity.getDepartamento())
                .ciudad(clubEntity.getCiudad())
                .fechaCreacion(clubEntity.getFechaCreacion())
                .activo(clubEntity.getActivo())
                .build();
    }

    /**
     * Crea un nuevo club sin asignar presidente.
     */
    @Override
    public ClubResponseDto createClub(@Valid ClubCreateRequestDto clubDto) {

        if (clubDto == null) {
            throw new IllegalArgumentException("El clubDto no puede ser null.");
        }

        ClubEntity clubEntity = clubRepository.save(
                ClubEntity
                        .builder()
                        .nombre(clubDto.nombre())
                        .departamento(clubDto.departamento())
                        .ciudad(clubDto.ciudad())
                        .fechaCreacion(LocalDate.now())
                        .activo(true)
                        .build()
        );

        return ClubResponseDto
                .builder()
                .id(clubEntity.getId())
                .nombre(clubEntity.getNombre())
                .departamento(clubEntity.getDepartamento())
                .ciudad(clubEntity.getCiudad())
                .fechaCreacion(clubEntity.getFechaCreacion())
                .activo(clubEntity.getActivo())
                .build();

    }

    /**
     * Actualiza los datos de un club identificado por {@code id}.
     */
    @Override
    public ClubResponseDto updateClub(long id, ClubEditRequestDto clubDto) {

        // 1. Usuario autenticado
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));

        // 2. Validar que sea presidente
        if (!"PRESIDENTE".equals(usuarioEntity.getRol().getNombre())) {
            throw new IllegalArgumentException("No tienes permiso para actualizar un club.");
        }

        // 3. Obtener club
        ClubEntity clubEntity = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club con id " + id + " no encontrado."));

        // 4. Validar que el presidente pertenece al club
        if (usuarioEntity.getClub() == null || !usuarioEntity.getClub().getId().equals(clubEntity.getId())) {
            throw new IllegalArgumentException("No tienes permiso para actualizar este club.");
        }

        // 5. Validar si el club está activo
        if (!Boolean.TRUE.equals(clubEntity.getActivo())) {
            throw new IllegalStateException("No se puede modificar un club desactivado por el representante distrital.");
        }

        // 6. Actualizar campos permitidos (solo no nulos)
        if (clubDto.nombre() != null) {
            clubEntity.setNombre(clubDto.nombre());
        }

        if (clubDto.departamento() != null) {
            clubEntity.setDepartamento(clubDto.departamento());
        }

        if (clubDto.ciudad() != null) {
            clubEntity.setCiudad(clubDto.ciudad());
        }

        // 7. Guardar cambios
        ClubEntity updated = clubRepository.save(clubEntity);

        // 8. Respuesta
        return ClubResponseDto.builder()
                .id(updated.getId())
                .nombre(updated.getNombre())
                .departamento(updated.getDepartamento())
                .ciudad(updated.getCiudad())
                .fechaCreacion(updated.getFechaCreacion())
                .activo(updated.getActivo())
                .build();
    }

    /**
     * Desactiva (marca como inactivo) el club identificado por {@code id}.
     *
     * Lógica de negocio:
     * - Marca el club como inactivo.
     * - Obtiene a todos los usuarios del club (incluyendo presidente).
     * - Cancela sus inscripciones activas (ACEPTADAS / PENDIENTES) en convocatorias.
     * - Cambia su rol a INTERESADO, limpia la referencia al club y cierra sus sesiones (tokenVersion++).
     * - Notifica a cada usuario (correo + WebSocket) indicando la desactivación y el cambio de rol.
     */
    @Override
    @Transactional
    public ClubResponseDto desactivateClub(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que 0.");
        }

        ClubEntity clubEntity = clubRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
        );

        // 1. Desactivar club
        clubEntity.setActivo(false);
        ClubEntity updatedClub = clubRepository.save(clubEntity);

        // 2. Obtener todos los usuarios del club
        List<UsuarioEntity> usuariosClub = usuarioRepository.findByClubId(updatedClub.getId());

        if (!usuariosClub.isEmpty()) {

            // 3. Rol INTERESADO
            RolEntity rolInteresado = usuarioRepository.findRolByNombre("INTERESADO")
                    .orElseThrow(() -> new IllegalStateException("Rol INTERESADO no encontrado."));

            // 4. Procesar cada usuario
            for (UsuarioEntity usuario : usuariosClub) {

                // 4.1 Cancelar inscripciones activas (ACEPTADAS / PENDIENTES)
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
                    if (conv != null) {
                        conv.setInscritos(conv.getInscritos() - 1);
                    }
                }

                inscripcionRepository.saveAll(inscripciones);

                // 4.2 Cambiar rol a INTERESADO, quitar club y cerrar sesión previa
                usuario.setRol(rolInteresado);
                usuario.setClub(null);
                incrementarTokenVersion(usuario);

                usuarioRepository.save(usuario);
            }

            // 5. Notificar por correo y WebSocket a todos los usuarios del club
            notificarDesactivacionClub(updatedClub, usuariosClub);
        }

        // 6. DTO de respuesta
        return ClubResponseDto
                .builder()
                .id(updatedClub.getId())
                .nombre(updatedClub.getNombre())
                .departamento(updatedClub.getDepartamento())
                .ciudad(updatedClub.getCiudad())
                .fechaCreacion(updatedClub.getFechaCreacion())
                .activo(updatedClub.getActivo())
                .build();
    }

    /**
     * Crea un club y asigna un presidente.
     * Cierra la sesión previa del usuario que asume la presidencia (tokenVersion++).
     */
    public ClubResponseDto createClubWithPresidente(ClubCreateWithPresidenteRequestDto dto) {

        // 1. Validar usuario
        UsuarioEntity presidente = usuarioRepository.findById(dto.presidenteId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario presidente no existe."));

        // 2. Validar que aún no pertenece a un club
        if (presidente.getClub() != null) {
            throw new IllegalStateException("Este usuario ya pertenece a un club.");
        }

        // 3. Crear club
        ClubEntity club = ClubEntity.builder()
                .nombre(dto.nombre())
                .departamento(dto.departamento())
                .ciudad(dto.ciudad())
                .fechaCreacion(LocalDate.now())
                .activo(true)
                .build();

        ClubEntity newClub = clubRepository.save(club);

        // 4. Asignar club al usuario
        presidente.setClub(newClub);

        // 5. Cambiar rol a PRESIDENTE y cerrar sesión previa
        RolEntity rolPresidente = usuarioRepository.findRolByNombre("PRESIDENTE")
                .orElseThrow(() -> new IllegalArgumentException("Rol PRESIDENTE no encontrado."));
        presidente.setRol(rolPresidente);
        incrementarTokenVersion(presidente);

        usuarioRepository.save(presidente);

        // 6. Respuesta DTO
        return ClubResponseDto.builder()
                .id(newClub.getId())
                .nombre(newClub.getNombre())
                .departamento(newClub.getDepartamento())
                .ciudad(newClub.getCiudad())
                .fechaCreacion(newClub.getFechaCreacion())
                .activo(newClub.getActivo())
                .build();
    }

    /**
     * Elimina un socio del club, lo devuelve a INTERESADO y cancela sus inscripciones.
     * Notifica al socio por correo y WebSocket.
     */
    public void removeSocioFromClub(Long clubId, Long socioId) {

        // Usuario autenticado
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity presidente = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        // Validar que es presidente
        if (!"PRESIDENTE".equals(presidente.getRol().getNombre())) {
            throw new IllegalArgumentException("No tienes permisos para gestionar socios.");
        }

        // Validar club
        ClubEntity club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("El club no existe."));

        // Validar que el presidente pertenece al club
        if (presidente.getClub() == null || !presidente.getClub().getId().equals(clubId)) {
            throw new IllegalArgumentException("No puedes administrar un club que no diriges.");
        }

        // Usuario a eliminar
        UsuarioEntity socio = usuarioRepository.findById(socioId)
                .orElseThrow(() -> new IllegalArgumentException("El socio no existe."));

        // Validar que pertenece al club
        if (socio.getClub() == null || !socio.getClub().getId().equals(clubId)) {
            throw new IllegalArgumentException("Ese usuario no pertenece a tu club.");
        }

        // No eliminar al presidente
        if ("PRESIDENTE".equals(socio.getRol().getNombre())) {
            throw new IllegalArgumentException("No puedes eliminar al presidente del club.");
        }

        // Cancelar inscripciones en convocatorias (ACEPTADAS / PENDIENTES)
        List<InscripcionEntity> inscripciones = inscripcionRepository
                .findByUsuarioId(socio.getId())
                .stream()
                .filter(i -> i.getConvocatoria() != null)
                .filter(i -> i.getEstado() == InscripcionEntity.EstadoInscripcion.ACEPTADA
                        || i.getEstado() == InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .toList();

        for (InscripcionEntity ins : inscripciones) {
            ins.setEstado(InscripcionEntity.EstadoInscripcion.CANCELADA);

            var conv = ins.getConvocatoria();
            if (conv != null) {
                conv.setInscritos(conv.getInscritos() - 1);
            }
        }

        inscripcionRepository.saveAll(inscripciones);

        // Cambiar rol a INTERESADO, quitar club y cerrar sesión previa
        RolEntity rolInteresado = usuarioRepository.findRolByNombre("INTERESADO")
                .orElseThrow(() -> new IllegalStateException("Rol INTERESADO no encontrado."));

        socio.setRol(rolInteresado);
        socio.setClub(null);
        incrementarTokenVersion(socio);

        usuarioRepository.save(socio);

        // Notificar al socio removido (correo + WebSocket)
        notificarRemocionSocio(socio, club, presidente);
    }

    /**
     * Transfiere la presidencia a otro socio del mismo club.
     * Cierra las sesiones de ambos (tokenVersion++) y notifica a ambos via correo y WebSocket.
     */
    public void transferirPresidencia(Long clubId, Long nuevoPresidenteId) {

        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity actualPresidente = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        if (!"PRESIDENTE".equals(actualPresidente.getRol().getNombre())) {
            throw new IllegalArgumentException("No tienes permiso para transferir la presidencia.");
        }

        if (actualPresidente.getClub() == null || !actualPresidente.getClub().getId().equals(clubId)) {
            throw new IllegalArgumentException("No perteneces a este club.");
        }

        ClubEntity club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club no encontrado."));

        UsuarioEntity nuevoPresidente = usuarioRepository.findById(nuevoPresidenteId)
                .orElseThrow(() -> new IllegalArgumentException("Nuevo presidente no encontrado."));

        if (nuevoPresidente.getClub() == null || !nuevoPresidente.getClub().getId().equals(clubId)) {
            throw new IllegalArgumentException("El nuevo presidente no pertenece al club.");
        }

        // Roles
        RolEntity rolPresidente = usuarioRepository.findRolByNombre("PRESIDENTE")
                .orElseThrow(() -> new IllegalArgumentException("Rol PRESIDENTE no encontrado."));

        RolEntity rolSocio = usuarioRepository.findRolByNombre("SOCIO")
                .orElseThrow(() -> new IllegalArgumentException("Rol SOCIO no encontrado."));

        // Cambiar roles
        actualPresidente.setRol(rolSocio);
        nuevoPresidente.setRol(rolPresidente);

        // Cerrar sesiones de ambos al cambiar rol
        incrementarTokenVersion(actualPresidente);
        incrementarTokenVersion(nuevoPresidente);

        usuarioRepository.save(actualPresidente);
        usuarioRepository.save(nuevoPresidente);

        // Notificar a ambos (correo + WebSocket)
        notificarTransferenciaPresidencia(actualPresidente, nuevoPresidente, club);
    }

    /**
     * Incrementa la versión de token de un usuario para invalidar sus sesiones previas.
     */
    private void incrementarTokenVersion(UsuarioEntity usuario) {
        Integer versionActual = usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion();
        usuario.setTokenVersion(versionActual + 1);
    }

    /**
     * Notifica la desactivación de un club a todos sus usuarios.
     *
     * Envía:
     * - Correo electrónico indicando que el club fue desactivado y que su rol ahora es INTERESADO.
     * - Notificación WebSocket al canal del usuario.
     */
    private void notificarDesactivacionClub(ClubEntity club, List<UsuarioEntity> usuariosClub) {

        String asunto = "Club desactivado - Rotaract D4465";

        for (UsuarioEntity usuario : usuariosClub) {

            String mensajeCorreo = """
                    <h2>Notificación de desactivación de club</h2>
                    <p>Le informamos que el club <strong>%s</strong> (%s - %s) ha sido desactivado a nivel distrital.</p>
                    <p>A partir de este momento, el club ya no se considera activo en la plataforma Rotaract D4465.</p>
                    <p>Su rol ha sido actualizado a <strong>INTERESADO</strong> y ya no se encuentra asociado a ningún club.</p>
                    """.formatted(
                    club.getNombre(),
                    club.getDepartamento(),
                    club.getCiudad()
            );

            // Correo
            emailService.enviarCorreo(
                    usuario.getCorreo(),
                    asunto,
                    mensajeCorreo
            );

            // WebSocket
            notificacionService.enviarAUsuario(
                    usuario.getId(),
                    new NotificacionDto(
                            "Club desactivado",
                            "El club " + club.getNombre() + " ha sido desactivado. Ahora eres INTERESADO y ya no perteneces a ningún club."
                    )
            );
        }
    }

    /**
     * Notifica al socio removido de un club.
     */
    private void notificarRemocionSocio(UsuarioEntity socio, ClubEntity club, UsuarioEntity presidente) {

        String asunto = "Ha sido removido del club - Rotaract D4465";

        String html = """
                <h2>Remoción de club</h2>
                <p>Le informamos que ha sido removido del club <strong>%s</strong> (%s - %s).</p>
                <p>La acción fue realizada por el presidente del club: %s.</p>
                <p>Su rol ha sido actualizado a <strong>INTERESADO</strong> en la plataforma.</p>
                """.formatted(
                club.getNombre(),
                club.getDepartamento(),
                club.getCiudad(),
                presidente.getNombre()
        );

        emailService.enviarCorreo(
                socio.getCorreo(),
                asunto,
                html
        );

        notificacionService.enviarAUsuario(
                socio.getId(),
                new NotificacionDto(
                        "Remoción de club",
                        "Ha sido removido del club " + club.getNombre() +
                                ". Su rol ahora es INTERESADO."
                )
        );
    }

    /**
     * Notifica la transferencia de presidencia a nuevo presidente y expresidente.
     */
    private void notificarTransferenciaPresidencia(UsuarioEntity anterior, UsuarioEntity nuevo, ClubEntity club) {

        // Correo nuevo presidente
        String asuntoNuevo = "Ha sido designado Presidente de club - Rotaract D4465";
        String htmlNuevo = """
                <h2>Felicitaciones, %s</h2>
                <p>Ha sido designado como <strong>Presidente</strong> del club <strong>%s</strong> (%s - %s).</p>
                <p>La presidencia le ha sido transferida por %s.</p>
                """.formatted(
                nuevo.getNombre(),
                club.getNombre(),
                club.getDepartamento(),
                club.getCiudad(),
                anterior.getNombre()
        );

        emailService.enviarCorreo(
                nuevo.getCorreo(),
                asuntoNuevo,
                htmlNuevo
        );

        // Correo expresidente
        String asuntoAnterior = "Transferencia de presidencia confirmada - Rotaract D4465";
        String htmlAnterior = """
                <h2>Hola %s</h2>
                <p>Confirmamos que ha transferido la <strong>Presidencia</strong> del club <strong>%s</strong> (%s - %s)</p>
                <p>al usuario <strong>%s</strong>.</p>
                <p>Su rol ha sido actualizado a <strong>SOCIO</strong>.</p>
                """.formatted(
                anterior.getNombre(),
                club.getNombre(),
                club.getDepartamento(),
                club.getCiudad(),
                nuevo.getNombre()
        );

        emailService.enviarCorreo(
                anterior.getCorreo(),
                asuntoAnterior,
                htmlAnterior
        );

        // WebSocket nuevo
        notificacionService.enviarAUsuario(
                nuevo.getId(),
                new NotificacionDto(
                        "Nueva presidencia asignada",
                        "Ahora es presidente del club " + club.getNombre() + "."
                )
        );

        // WebSocket anterior
        notificacionService.enviarAUsuario(
                anterior.getId(),
                new NotificacionDto(
                        "Transferencia de presidencia realizada",
                        "Ha transferido la presidencia del club " + club.getNombre() +
                                " a " + nuevo.getNombre() + "."
                )
        );
    }
}
