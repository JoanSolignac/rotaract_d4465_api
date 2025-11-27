package com.unapi.rotaract.rotaract_d4465_api.asistencia.service;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaGuardarRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.AsistenciaResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.dtos.HistorialAsistenciaDto;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.interfaces.IAsistenciaService;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.repository.AsistenciaRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements IAsistenciaService {

    private final ProyectoRepository proyectoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    // ============================================================
    // UTILIDAD: USUARIO AUTENTICADO
    // ============================================================

    private UsuarioEntity getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));
    }

    // ============================================================
    // 1. LISTAR SOCIOS PARA MARCAR ASISTENCIA
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarSociosParaAsistencia(Long proyectoId) {

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        if (!proyecto.puedeRegistrarAsistencia()) {
            throw new IllegalStateException("La asistencia no está activa para este proyecto.");
        }

        UsuarioEntity presidente = getUsuarioAutenticado();
        if (proyecto.getClub() == null || presidente.getClub() == null ||
                !proyecto.getClub().getId().equals(presidente.getClub().getId())) {
            throw new IllegalStateException("No puedes gestionar la asistencia de un proyecto que no pertenece a tu club.");
        }

        List<InscripcionEntity> inscripciones = inscripcionRepository.findByProyectoId(proyectoId);

        return inscripciones.stream().map(ins -> {

            Long usuarioId = ins.getUsuario().getId();

            AsistenciaEntity asistencia = asistenciaRepository
                    .findByProyectoIdAndUsuarioId(proyectoId, usuarioId)
                    .orElse(null);

            boolean presente = asistencia != null &&
                    asistencia.getEstado() == AsistenciaEntity.EstadoAsistencia.PRESENTE;

            UsuarioEntity u = ins.getUsuario();

            return AsistenciaResponseDto.builder()
                    .usuarioId(u.getId())
                    .usuarioNombreCompleto(u.getNombre())
                    .usuarioCorreo(u.getCorreo())
                    .proyectoId(proyectoId)
                    .presente(presente)
                    .build();

        }).toList();
    }

    // ============================================================
    // 2. GUARDAR ASISTENCIA
    // ============================================================

    @Override
    @Transactional
    public void guardarAsistencia(Long proyectoId, AsistenciaGuardarRequestDto dto) {

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        if (!proyecto.puedeRegistrarAsistencia()) {
            throw new IllegalStateException("La asistencia no está activa para este proyecto.");
        }

        UsuarioEntity presidente = getUsuarioAutenticado();
        if (proyecto.getClub() == null || presidente.getClub() == null ||
                !proyecto.getClub().getId().equals(presidente.getClub().getId())) {
            throw new IllegalStateException("No puedes registrar la asistencia de un proyecto que no pertenece a tu club.");
        }

        List<Long> presentesIds = dto.usuariosPresentesIds();
        List<InscripcionEntity> inscripciones = inscripcionRepository.findByProyectoId(proyectoId);

        for (InscripcionEntity ins : inscripciones) {

            Long usuarioId = ins.getUsuario().getId();
            boolean esPresente = presentesIds.contains(usuarioId);

            AsistenciaEntity asistencia = asistenciaRepository
                    .findByProyectoIdAndUsuarioId(proyectoId, usuarioId)
                    .orElse(null);

            if (asistencia == null) {
                asistencia = AsistenciaEntity.builder()
                        .usuario(ins.getUsuario())
                        .proyecto(proyecto)
                        .estado(esPresente
                                ? AsistenciaEntity.EstadoAsistencia.PRESENTE
                                : AsistenciaEntity.EstadoAsistencia.FALTA)
                        .fechaRegistro(LocalDateTime.now())
                        .build();
            } else {
                asistencia.setEstado(esPresente
                        ? AsistenciaEntity.EstadoAsistencia.PRESENTE
                        : AsistenciaEntity.EstadoAsistencia.FALTA);
                asistencia.setFechaRegistro(LocalDateTime.now());
            }

            asistenciaRepository.save(asistencia);
        }
    }

    // ============================================================
    // 3. LISTAR ASISTENCIAS PARA VER (NO EDITAR)
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarAsistenciasDeProyecto(Long proyectoId) {

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        UsuarioEntity presidente = getUsuarioAutenticado();
        if (proyecto.getClub() == null || presidente.getClub() == null ||
                !proyecto.getClub().getId().equals(presidente.getClub().getId())) {
            throw new IllegalStateException("No puedes ver la asistencia de un proyecto que no pertenece a tu club.");
        }

        List<AsistenciaEntity> asistencias = asistenciaRepository.findByProyectoId(proyectoId);

        if (asistencias == null) asistencias = Collections.emptyList();

        return asistencias.stream()
                .map(this::mapAsistenciaToResponse)
                .toList();
    }

    // ============================================================
    // 4. MÉTODO ANTIGUO: MIS ASISTENCIAS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> obtenerMisAsistencias() {

        UsuarioEntity usuario = getUsuarioAutenticado();

        List<AsistenciaEntity> asistencias = asistenciaRepository.findByUsuarioId(usuario.getId());

        return asistencias.stream()
                .map(this::mapMisAsistencias)
                .toList();
    }

    // ============================================================
    // 5. HISTORIAL DETALLADO
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<HistorialAsistenciaDto> obtenerHistorialAsistencias() {

        UsuarioEntity usuario = getUsuarioAutenticado();

        List<AsistenciaEntity> asistencias = asistenciaRepository.findByUsuarioId(usuario.getId());

        return asistencias.stream()
                .sorted((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()))
                .map(this::mapHistorial)
                .toList();
    }

    // ============================================================
    // 6. EXPORTAR ASISTENCIAS A EXCEL
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcelAsistencias(Long proyectoId) {

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));

        UsuarioEntity presidente = getUsuarioAutenticado();

        if (proyecto.getClub() == null ||
                presidente.getClub() == null ||
                !proyecto.getClub().getId().equals(presidente.getClub().getId())) {

            throw new IllegalStateException("No puedes exportar la asistencia de un proyecto que no pertenece a tu club.");
        }

        List<AsistenciaEntity> asistencias = asistenciaRepository.findByProyectoId(proyectoId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("Asistencias");

            // ====== CABECERA ======
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID Asistencia");
            header.createCell(1).setCellValue("Usuario");
            header.createCell(2).setCellValue("Correo");
            header.createCell(3).setCellValue("Estado");
            header.createCell(4).setCellValue("Fecha Registro");

            int rowNum = 1;

            for (AsistenciaEntity a : asistencias) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(a.getId());
                row.createCell(1).setCellValue(a.getUsuario().getNombre());
                row.createCell(2).setCellValue(a.getUsuario().getCorreo());
                row.createCell(3).setCellValue(a.getEstado().name());
                row.createCell(4).setCellValue(a.getFechaRegistro().toString());
            }

            // Autoajustar columnas
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // MAPPERS
    // ============================================================

    private AsistenciaResponseDto mapAsistenciaToResponse(AsistenciaEntity asistencia) {

        UsuarioEntity u = asistencia.getUsuario();
        Long proyectoId = asistencia.getProyecto() != null
                ? asistencia.getProyecto().getId()
                : null;

        boolean presente =
                asistencia.getEstado() == AsistenciaEntity.EstadoAsistencia.PRESENTE;

        return AsistenciaResponseDto.builder()
                .usuarioId(u.getId())
                .usuarioNombreCompleto(u.getNombre())
                .usuarioCorreo(u.getCorreo())
                .proyectoId(proyectoId)
                .presente(presente)
                .build();
    }

    private AsistenciaResponseDto mapMisAsistencias(AsistenciaEntity asistencia) {

        ProyectoEntity p = asistencia.getProyecto();

        boolean presente = asistencia.getEstado() == AsistenciaEntity.EstadoAsistencia.PRESENTE;

        return AsistenciaResponseDto.builder()
                .usuarioId(null)
                .usuarioNombreCompleto(null)
                .usuarioCorreo(null)
                .proyectoId(p != null ? p.getId() : null)
                .presente(presente)
                .build();
    }

    private HistorialAsistenciaDto mapHistorial(AsistenciaEntity asistencia) {

        ProyectoEntity p = asistencia.getProyecto();

        return new HistorialAsistenciaDto(
                asistencia.getId(),
                p != null ? p.getId() : null,
                p != null ? p.getTitulo() : null,
                asistencia.getEstado().name(),
                asistencia.getFechaRegistro()
        );
    }
}
