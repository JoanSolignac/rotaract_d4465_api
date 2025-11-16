package com.unapi.rotaract.rotaract_d4465_api.proyecto.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.dtos.ProyectoResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interfaz que define las operaciones principales para la administración
 * de proyectos dentro del sistema. Proporciona la estructura de métodos
 * utilizada por la capa de servicio para gestionar consultas, registro
 * y modificación de proyectos, devolviendo siempre estructuras DTO
 * adecuadas para exposición pública.
 */
public interface IProyectoService {

    /**
     * Recupera una lista paginada de proyectos.
     * Permite controlar el número de elementos consultados y el desplazamiento
     * dentro del conjunto total de registros.
     *
     * @param page número de página (0-based)
     * @param size cantidad máxima de elementos por página
     * @return página de proyectos representados como {@link ProyectoResponseDto}
     */
    Page<ProyectoResponseDto> findAll(int page, int size);

    /**
     * Obtiene los datos de un proyecto específico identificado por su ID.
     * En caso de no existir, la implementación deberá gestionar la excepción.
     *
     * @param id identificador del proyecto
     * @return representación pública del proyecto
     */
    ProyectoResponseDto findById(Long id);

    /**
     * Registra un nuevo proyecto dentro del sistema.
     * La implementación valida los datos y gestiona las reglas de negocio
     * asociadas al ciclo de vida del proyecto.
     *
     * @param dto datos requeridos para crear un nuevo proyecto
     * @return información del proyecto recién creado
     */
    ProyectoResponseDto create(ProyectoCreateRequestDto dto);

    /**
     * Actualiza parcialmente un proyecto ya existente. Sólo se modifican
     * los campos proporcionados en el DTO, manteniendo sin cambios
     * aquellos que se envíen como null.
     *
     * @param id identificador del proyecto a actualizar
     * @param dto datos editables del proyecto
     * @return representación del proyecto luego de la actualización
     */
    ProyectoResponseDto update(Long id, ProyectoEditRequestDto dto);

    // ======================================================
    // =               CANCELAR PROYECTO                    =
    // ======================================================
    @Transactional
    void cancelarProyecto(Long id);

    // ======================================================
    // =              FINALIZAR PROYECTO                    =
    // ======================================================
    @Transactional
    void finalizarProyecto(Long id);

    // ======================================================
    // =            BUSCAR POR TÍTULO                       =
    // ======================================================
    Page<ProyectoResponseDto> buscarPorTitulo(String titulo, int page, int size);
}
