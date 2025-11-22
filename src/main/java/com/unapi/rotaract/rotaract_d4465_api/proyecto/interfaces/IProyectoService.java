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
     * Recupera una lista paginada de todos los proyectos.
     */
    Page<ProyectoResponseDto> findAll(int page, int size);

    /**
     * Lista los proyectos del club de un SOCIO o PRESIDENTE.
     */
    Page<ProyectoResponseDto> findAllBySocioPresidente(int page, int size);

    /**
     * Lista proyectos que el usuario actual NO ha inscrito.
     * — Solo aplica para SOCIO y PRESIDENTE.
     * — Permite mostrar únicamente los proyectos nuevos/disponibles.
     */
    Page<ProyectoResponseDto> findDisponiblesParaUsuario(int page, int size);

    /**
     * Obtiene los datos de un proyecto por su ID.
     */
    ProyectoResponseDto findById(Long id);

    /**
     * Crea un nuevo proyecto.
     */
    ProyectoResponseDto create(ProyectoCreateRequestDto dto);

    /**
     * Actualiza parcialmente un proyecto existente.
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
    // =               BUSCAR POR TÍTULO                    =
    // ======================================================
    Page<ProyectoResponseDto> buscarPorTitulo(String titulo, int page, int size);
}
