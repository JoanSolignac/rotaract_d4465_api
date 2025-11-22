package com.unapi.rotaract.rotaract_d4465_api.asistencia.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaGuardarRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaResponseDto;

import java.util.List;

public interface IAsistenciaService {

    List<AsistenciaResponseDto> listarSociosParaAsistencia(Long proyectoId);

    void guardarAsistencia(Long proyectoId, AsistenciaGuardarRequestDto dto);
}
