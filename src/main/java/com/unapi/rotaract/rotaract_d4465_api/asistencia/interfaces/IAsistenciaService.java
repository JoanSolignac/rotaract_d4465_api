package com.unapi.rotaract.rotaract_d4465_api.asistencia.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaGuardarRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.HistorialAsistenciaDto;

import java.util.List;

/**
 * Contrato del servicio de asistencias.
 *
 * Define operaciones para:
 * - Listar socios inscritos de un proyecto para marcar asistencia.
 * - Guardar la asistencia de un proyecto.
 * - Consultar las asistencias registradas de un proyecto.
 * - Consultar el historial de asistencias del usuario autenticado.
 */
public interface IAsistenciaService {

    /**
     * Lista los socios inscritos en un proyecto, indicando si están marcados
     * como presentes o no, para ser usado en la toma de asistencia.
     *
     * @param proyectoId identificador del proyecto
     * @return listado de DTOs con la información del usuario y su estado presente/falta
     */
    List<AsistenciaResponseDto> listarSociosParaAsistencia(Long proyectoId);

    /**
     * Guarda la asistencia (presentes y faltas) para un proyecto.
     *
     * @param proyectoId identificador del proyecto
     * @param dto        DTO que contiene los IDs de usuarios presentes
     */
    void guardarAsistencia(Long proyectoId, AsistenciaGuardarRequestDto dto);

    /**
     * Devuelve la lista de asistencias registradas para un proyecto.
     *
     * @param proyectoId identificador del proyecto
     * @return listado de asistencias registradas para el proyecto
     */
    List<AsistenciaResponseDto> listarAsistenciasDeProyecto(Long proyectoId);

    /**
     * Método original para obtener asistencias del usuario autenticado.
     * (Lo dejamos tal cual para no romper nada que ya lo use.)
     *
     * @return listado de asistencias propias del usuario actual
     */
    List<AsistenciaResponseDto> obtenerMisAsistencias();

    /**
     * NUEVO MÉTODO — Historial completo del usuario autenticado,
     * incluyendo detalle del proyecto, fecha y estado de asistencia.
     *
     * @return listado de asistencias del usuario autenticado en formato detallado
     */
    List<HistorialAsistenciaDto> obtenerHistorialAsistencias();

}
