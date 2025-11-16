package com.unapi.rotaract.rotaract_d4465_api.convocatoria.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces.IConvocatoriaService;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio responsable de gestionar el ciclo de vida de las convocatorias.
 * Incluye operaciones de creación, consulta, edición y validación de
 * reglas temporales asociadas al proceso de postulación.
 *
 * El club asociado a la convocatoria se obtiene automáticamente desde
 * el usuario autenticado, evitando parámetros manipulables.
 */
@Service
@RequiredArgsConstructor
public class ConvocatoriaServiceImpl implements IConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Recupera una lista paginada de convocatorias.
     *
     * @param page índice de página (0-based)
     * @param size tamaño máximo de elementos por página
     * @return página con {@link ConvocatoriaResponseDto}
     * @throws IllegalArgumentException si el tamaño de página es inválido
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ConvocatoriaResponseDto> findAll(int page, int size) {

        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        }

        return convocatoriaRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    /**
     * Recupera una convocatoria por su identificador.
     *
     * @param id identificador único
     * @return DTO con datos públicos de la convocatoria
     * @throws IllegalArgumentException si no existe la convocatoria
     */
    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaResponseDto findById(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que 0.");
        }

        ConvocatoriaEntity convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Convocatoria con id " + id + " no encontrada.")
                );

        return mapToResponse(convocatoria);
    }

    /**
     * Crea una nueva convocatoria asociada al club del usuario autenticado.
     *
     * @param dto datos requeridos para la creación
     * @return representación pública de la convocatoria creada
     * @throws IllegalStateException si el usuario no pertenece a un club
     * @throws IllegalArgumentException si las fechas son inválidas
     */
    @Override
    @Transactional
    public ConvocatoriaResponseDto create(ConvocatoriaCreateRequestDto dto) {


        String correo = SecurityContextHolder.getContext().getAuthentication().getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo).orElseThrow(
                () -> new IllegalStateException("Usuario autenticado no encontrado.")
        );
        ClubEntity club = usuario.getClub();

        if (club == null) {
            throw new IllegalStateException("El usuario autenticado no pertenece a ningún club.");
        }

        validarFechasCreacion(dto);

        ConvocatoriaEntity entity = ConvocatoriaEntity.builder()
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .club(club)
                .cupoMaximo(dto.cupoMaximo())
                .fechaPublicacion(dto.fechaPublicacion())
                .fechaCierre(dto.fechaCierre())
                .fechaInicioPostulacion(dto.fechaInicioPostulacion())
                .fechaFinPostulacion(dto.fechaFinPostulacion())
                .requisitos(dto.requisitos())
                .estado(EventoEntity.EstadoEvento.ACTIVO)
                .build();

        ConvocatoriaEntity persisted = convocatoriaRepository.save(entity);

        return mapToResponse(persisted);
    }

    /**
     * Actualiza parcialmente los campos editables de una convocatoria.
     *
     * @param id identificador de la convocatoria
     * @param dto campos opcionales a modificar
     * @return datos actualizados de la convocatoria
     * @throws IllegalArgumentException si la convocatoria no existe o si las fechas resultantes son inválidas
     */
    @Override
    @Transactional
    public ConvocatoriaResponseDto update(Long id, ConvocatoriaEditRequestDto dto) {

        ConvocatoriaEntity entity = convocatoriaRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Convocatoria con id " + id + " no encontrada.")
                );

        if (dto.titulo() != null) entity.setTitulo(dto.titulo());
        if (dto.descripcion() != null) entity.setDescripcion(dto.descripcion());
        if (dto.cupoMaximo() != null) entity.setCupoMaximo(dto.cupoMaximo());
        if (dto.fechaCierre() != null) entity.setFechaCierre(dto.fechaCierre());
        if (dto.fechaInicioPostulacion() != null) entity.setFechaInicioPostulacion(dto.fechaInicioPostulacion());
        if (dto.fechaFinPostulacion() != null) entity.setFechaFinPostulacion(dto.fechaFinPostulacion());
        if (dto.requisitos() != null) entity.setRequisitos(dto.requisitos());

        validarFechasEdicion(entity);

        ConvocatoriaEntity updated = convocatoriaRepository.save(entity);

        return mapToResponse(updated);
    }

    /**
     * Verifica la coherencia temporal de los valores proporcionados
     * durante la creación de una convocatoria.
     *
     * Reglas:
     * fechaPublicacion ≤ fechaInicioPostulacion
     * fechaInicioPostulacion ≤ fechaFinPostulacion
     * fechaFinPostulacion ≤ fechaCierre
     *
     * @param dto estructura con fechas a validar
     */
    private void validarFechasCreacion(ConvocatoriaCreateRequestDto dto) {

        if (dto.fechaInicioPostulacion().isBefore(dto.fechaPublicacion())) {
            throw new IllegalArgumentException("La postulación no puede iniciar antes de la publicación.");
        }

        if (dto.fechaFinPostulacion().isBefore(dto.fechaInicioPostulacion())) {
            throw new IllegalArgumentException("La fecha fin de postulación no puede ser anterior a la fecha de inicio.");
        }

        if (dto.fechaFinPostulacion().isAfter(dto.fechaCierre())) {
            throw new IllegalArgumentException("La postulación no puede terminar después del cierre.");
        }
    }

    /**
     * Verifica la coherencia temporal de la entidad resultante
     * después de aplicar ediciones.
     *
     * @param entity entidad modificada
     */
    private void validarFechasEdicion(ConvocatoriaEntity entity) {

        if (entity.getFechaInicioPostulacion().isBefore(entity.getFechaPublicacion())) {
            throw new IllegalArgumentException("La postulación no puede iniciar antes de la publicación.");
        }

        if (entity.getFechaFinPostulacion().isBefore(entity.getFechaInicioPostulacion())) {
            throw new IllegalArgumentException("La fecha fin de postulación no puede ser anterior a la fecha de inicio.");
        }

        if (entity.getFechaFinPostulacion().isAfter(entity.getFechaCierre())) {
            throw new IllegalArgumentException("La postulación no puede terminar después del cierre.");
        }
    }

    /**
     * Convierte una entidad en su representación de salida.
     *
     * @param c entidad persistida
     * @return DTO con información pública
     */
    private ConvocatoriaResponseDto mapToResponse(ConvocatoriaEntity c) {
        return new ConvocatoriaResponseDto(
                c.getId(),
                c.getTitulo(),
                c.getDescripcion(),
                c.getRequisitos(),
                c.getCupoMaximo(),
                c.getFechaPublicacion(),
                c.getFechaCierre(),
                c.getFechaInicioPostulacion(),
                c.getFechaFinPostulacion(),
                c.getClub().getId(),
                c.getClub().getNombre(),
                c.getEstado().name()
        );
    }
}
