package com.unapi.rotaract.rotaract_d4465_api.club.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.*;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.club.interfaces.IClubConsultaService;
import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Servicio de consulta de clubs encargado de construir una vista detallada del estado actual
 * de un club. Implementa {@link IClubConsultaService} y consolida información proveniente de:
 * - Club: datos básicos de identificación y localización.
 * - Presidente: usuario activo con rol PRESIDENTE (si existe).
 * - Métricas: totales de integrantes activos, convocatorias y proyectos.
 * - Integrantes paginados: usuarios activos del club excluyendo al presidente.
 * - Convocatorias recientes: ordenadas por inicio de postulación descendente.
 * - Proyectos recientes: ordenados por fecha de inicio descendente.
 *
 * Flujo interno del método principal obtenerDetalleClub:
 * 1. Recupera la entidad Club y valida existencia.
 * 2. Obtiene el presidente (rol = PRESIDENTE) si está activo.
 * 3. Calcula métricas agregadas (integrantes, convocatorias, proyectos).
 * 4. Paginación de integrantes activos (excluye presidente por rol).
 * 5. Carga y transforma convocatorias del club a DTO resumen.
 * 6. Carga y transforma proyectos del club a DTO resumen.
 * 7. Construye DTO resumen del club.
 * 8. Ensambla la respuesta final en un {@link ClubDetalleResponseDto}.
 *
 * Se utilizan builders para mantener inmutabilidad semántica y claridad en la construcción
 * de los distintos DTOs. El servicio no aplica reglas de negocio complejas; actúa como
 * un agregador/ensamblador de datos de lectura.
 */
public class ClubConsultaServiceImpl implements IClubConsultaService {

    /** Rol de presidente usado como criterio para separar líder del resto de integrantes. */
    private static final String ROL_PRESIDENTE = "PRESIDENTE";

    /** Acceso a la persistencia de clubs. */
    private final ClubRepository clubRepository;
    /** Acceso a usuarios para obtener presidente, integrantes y métricas. */
    private final UsuarioRepository usuarioRepository;
    /** Acceso a convocatorias para conteo y listado reciente. */
    private final ConvocatoriaRepository convocatoriaRepository;
    /** Acceso a proyectos para conteo y listado reciente. */
    private final ProyectoRepository proyectoRepository;

    @Override
    /**
     * Construye una vista completa del club solicitado, combinando datos estructurados y colecciones paginadas.
     *
     * Parámetros:
     * - clubId: identificador del club a consultar.
     * - page: número de página (0-based) para la lista de integrantes.
     * - size: tamaño de página para la lista de integrantes.
     *
     * Proceso:
     *  - Verifica existencia del club; si no existe lanza EntityNotFoundException.
     *  - Localiza presidente (rol PRESIDENTE) si está activo y lo mapea a UsuarioResumenDto.
     *  - Calcula métricas (integrantes activos, convocatorias, proyectos).
     *  - Obtiene integrantes paginados excluyendo presidente, mapeando cada uno a UsuarioResumenDto.
     *  - Obtiene convocatorias ordenadas por fecha de inicio de postulación descendente y las mapea.
     *  - Obtiene proyectos ordenados por fecha de inicio descendente y los mapea.
     *  - Mapea la entidad del club a ClubResumenDto.
     *  - Ensambla todo en ClubDetalleResponseDto para su retorno.
     *
     * Consideraciones:
     *  - Las listas pueden resultar vacías sin considerarse error.
     *  - El presidente puede ser nulo si no existe un usuario con ese rol activo.
     *  - La paginación de integrantes utiliza PageRequest.of(page, size) sin validaciones adicionales aquí;
     *    se asume que la capa superior controla límites razonables.
     *
     * @return DTO que encapsula la información consolidada del club.
     */
    public ClubDetalleResponseDto obtenerDetalleClub(Long clubId, int page, int size) {

        // 1. Club
        ClubEntity club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club no encontrado"));

        // 2. Presidente
        UsuarioEntity presidente = usuarioRepository
                .findFirstByClubIdAndRol_NombreAndActivoTrue(clubId, ROL_PRESIDENTE)
                .orElse(null);

        UsuarioResumenDto presidenteDto = (presidente == null)
                ? null
                : UsuarioResumenDto.builder()
                .id(presidente.getId())
                .nombre(presidente.getNombre())
                .correo(presidente.getCorreo())
                .build();

        // 3. Métricas
        long totalIntegrantes = usuarioRepository.countByClubIdAndActivoTrue(clubId);
        long totalConvocatorias = convocatoriaRepository.countByClubId(clubId);
        long totalProyectos = proyectoRepository.countByClubId(clubId);

        ClubMetricasDto metricasDto = ClubMetricasDto.builder()
                .totalIntegrantes(totalIntegrantes)
                .totalConvocatorias(totalConvocatorias)
                .totalProyectos(totalProyectos)
                .build();

        // 4. Integrantes paginados (sin presidente)
        Page<UsuarioEntity> pageIntegrantes =
                usuarioRepository.findByClubIdAndActivoTrueAndRol_NombreNot(
                        clubId,
                        ROL_PRESIDENTE,
                        PageRequest.of(page, size)
                );

        List<UsuarioResumenDto> integrantesContent = pageIntegrantes.getContent()
                .stream()
                .map(u -> UsuarioResumenDto.builder()
                        .id(u.getId())
                        .nombre(u.getNombre())
                        .correo(u.getCorreo())
                        .build())
                .collect(Collectors.toList());

        PaginacionIntegrantesDto integrantesDto = PaginacionIntegrantesDto.builder()
                .content(integrantesContent)
                .page(pageIntegrantes.getNumber())
                .size(pageIntegrantes.getSize())
                .totalElements(pageIntegrantes.getTotalElements())
                .totalPages(pageIntegrantes.getTotalPages())
                .first(pageIntegrantes.isFirst())
                .last(pageIntegrantes.isLast())
                .build();

        // 5. Convocatorias del club
        List<ConvocatoriaEntity> convocatoriasEntities =
                convocatoriaRepository.findByClubIdOrderByFechaInicioPostulacionDesc(clubId);

        List<ConvocatoriaResumenDto> convocatorias = convocatoriasEntities.stream()
                .map(c -> ConvocatoriaResumenDto.builder()
                        .id(c.getId())
                        .titulo(c.getTitulo())
                        .estado(c.getEstado().name())
                        .fechaInicioPostulacion(c.getFechaInicioPostulacion())
                        .fechaFinPostulacion(c.getFechaFinPostulacion())
                        .build())
                .collect(Collectors.toList());

        // 6. Proyectos del club
        List<ProyectoEntity> proyectosEntities =
                proyectoRepository.findByClubIdOrderByFechaInicioProyectoDesc(clubId);

        List<ProyectoResumenDto> proyectos = proyectosEntities.stream()
                .map(p -> ProyectoResumenDto.builder()
                        .id(p.getId())
                        .titulo(p.getTitulo())
                        .estado(p.getEstadoProyecto().name())
                        .fechaInicioProyecto(p.getFechaInicioProyecto())
                        .fechaFinProyecto(p.getFechaFinProyecto())
                        .build())
                .collect(Collectors.toList());

        // 7. Club resumen
        ClubResumenDto clubDto = ClubResumenDto.builder()
                .id(club.getId())
                .nombre(club.getNombre())
                .departamento(club.getDepartamento())
                .ciudad(club.getCiudad())
                .fechaCreacion(club.getFechaCreacion())
                .activo(club.getActivo())
                .build();

        // 8. Respuesta final
        return ClubDetalleResponseDto.builder()
                .club(clubDto)
                .presidente(presidenteDto)
                .metricas(metricasDto)
                .integrantes(integrantesDto)
                .convocatorias(convocatorias)
                .proyectos(proyectos)
                .build();
    }

    @Override
    /**
     * Obtiene las métricas y detalles del club del presidente autenticado.
     *
     * Proceso:
     *  - Busca al usuario presidente por su correo y verifica que tenga el rol PRESIDENTE.
     *  - Verifica que el presidente tenga un club asignado.
     *  - Delega al método obtenerDetalleClub para construir la respuesta completa.
     *
     * @param correoPresidente correo del presidente autenticado
     * @param page número de página para la paginación de integrantes
     * @param size tamaño de página para la paginación de integrantes
     * @return DTO con información detallada del club del presidente
     * @throws EntityNotFoundException si el usuario no existe, no es presidente o no tiene club asignado
     */
    public ClubDetalleResponseDto obtenerMetricasClubPresidente(String correoPresidente, int page, int size) {
        // 1. Buscar al presidente por correo
        UsuarioEntity presidente = usuarioRepository.findByCorreoAndActivoTrue(correoPresidente)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // 2. Verificar que tenga el rol PRESIDENTE
        if (presidente.getRol() == null || !ROL_PRESIDENTE.equals(presidente.getRol().getNombre())) {
            throw new EntityNotFoundException("El usuario no tiene el rol de PRESIDENTE");
        }

        // 3. Verificar que tenga un club asignado
        if (presidente.getClub() == null) {
            throw new EntityNotFoundException("El presidente no tiene un club asignado");
        }

        // 4. Obtener el detalle completo del club
        return obtenerDetalleClub(presidente.getClub().getId(), page, size);
    }
}
