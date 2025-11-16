package com.unapi.rotaract.rotaract_d4465_api.proyecto.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.interfaces.IProyectoService;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Recupera una página de proyectos existentes en el sistema.
     * Permite obtener bloques de información paginada para optimizar consultas.
     */
    @Override
    public Page<ProyectoResponseDto> findAll(int page, int size) {
        return proyectoRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    /**
     * Obtiene la información completa de un proyecto mediante su identificador.
     * Si no existe, se informa al usuario mediante una excepción controlada.
     */
    @Override
    public ProyectoResponseDto findById(Long id) {
        ProyectoEntity entity = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));
        return mapToResponse(entity);
    }

    /**
     * Registra un nuevo proyecto en el sistema. El usuario autenticado debe
     * pertenecer a un club, el cual se asigna automáticamente al proyecto.
     * La operación respeta las reglas básicas del ciclo de vida del proyecto.
     */
    @Override
    @Transactional
    public ProyectoResponseDto create(ProyectoCreateRequestDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

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

        proyecto.setObjetivo(dto.objetivo());
        proyecto.setFechaInicioPostulacion(dto.fechaInicioPostulacion());
        proyecto.setFechaFinPostulacion(dto.fechaFinPostulacion());
        proyecto.setFechaInicioProyecto(dto.fechaInicioProyecto());
        proyecto.setFechaFinProyecto(dto.fechaFinProyecto());
        proyecto.setEstadoProyecto(ProyectoEntity.EstadoProyecto.EN_POSTULACION);

        proyectoRepository.save(proyecto);

        return mapToResponse(proyecto);
    }

    /**
     * Actualiza parcialmente los datos de un proyecto existente.
     * Solo los campos presentes en el DTO serán modificados, manteniendo
     * intactos aquellos que no hayan sido enviados.
     */
    @Override
    @Transactional
    public ProyectoResponseDto update(Long id, ProyectoEditRequestDto dto) {

        ProyectoEntity p = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));

        if (dto.descripcion() != null) p.setDescripcion(dto.descripcion());
        if (dto.requisitos() != null) p.setRequisitos(dto.requisitos());
        if (dto.lugar() != null) p.setLugar(dto.lugar());

        if (dto.objetivo() != null) p.setObjetivo(dto.objetivo());
        if (dto.fechaInicioPostulacion() != null) p.setFechaInicioPostulacion(dto.fechaInicioPostulacion());
        if (dto.fechaFinPostulacion() != null) p.setFechaFinPostulacion(dto.fechaFinPostulacion());
        if (dto.fechaInicioProyecto() != null) p.setFechaInicioProyecto(dto.fechaInicioProyecto());
        if (dto.fechaFinProyecto() != null) p.setFechaFinProyecto(dto.fechaFinProyecto());

        proyectoRepository.save(p);

        return mapToResponse(p);
    }

    /**
     * Cancela un proyecto existente, estableciendo su estado general
     * y su estado específico del ciclo de vida como cancelado.
     */
    @Override
    @Transactional
    public void cancelarProyecto(Long id) {
        ProyectoEntity p = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));

        p.setEstado(EventoEntity.EstadoEvento.CANCELADO);
        p.setEstadoProyecto(ProyectoEntity.EstadoProyecto.CANCELADO);

        proyectoRepository.save(p);
    }

    /**
     * Marca un proyecto como finalizado, indicando que su ciclo de vida
     * ha concluido correctamente y cerrando su estado de evento.
     */
    @Override
    @Transactional
    public void finalizarProyecto(Long id) {
        ProyectoEntity p = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado."));

        p.setEstado(EventoEntity.EstadoEvento.CERRADO);
        p.setEstadoProyecto(ProyectoEntity.EstadoProyecto.FINALIZADO);

        proyectoRepository.save(p);
    }

    /**
     * Busca proyectos que contengan cierta cadena en su título, sin diferenciar
     * entre mayúsculas y minúsculas. Devuelve los resultados paginados.
     */
    @Override
    public Page<ProyectoResponseDto> buscarPorTitulo(String titulo, int page, int size) {
        return proyectoRepository
                .findByTituloContainingIgnoreCase(titulo, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    /**
     * Convierte una entidad de proyecto en una estructura DTO preparada para
     * la exposición pública mediante la API, evitando exponer entidades internas.
     */
    private ProyectoResponseDto mapToResponse(ProyectoEntity e) {
        return new ProyectoResponseDto(
                e.getId(),
                e.getTitulo(),
                e.getDescripcion(),
                e.getObjetivo(),
                e.getRequisitos(),
                e.getLugar(),
                e.getFechaInicioPostulacion(),
                e.getFechaFinPostulacion(),
                e.getFechaInicioProyecto(),
                e.getFechaFinProyecto(),
                e.getClub() != null ? e.getClub().getId() : null,
                e.getClub() != null ? e.getClub().getNombre() : null,
                e.getEstadoProyecto().name()
        );
    }
}
