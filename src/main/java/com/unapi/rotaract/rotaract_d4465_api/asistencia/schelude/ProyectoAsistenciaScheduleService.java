package com.unapi.rotaract.rotaract_d4465_api.asistencia.schelude;

import com.unapi.rotaract.rotaract_d4465_api.asistencia.entity.AsistenciaEntity;
import com.unapi.rotaract.rotaract_d4465_api.asistencia.repository.AsistenciaRepository;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler encargado del ciclo de vida de la asistencia:
 *
 * 1. ACTIVAR asistencia al inicio del proyecto.
 * 2. CREAR registros NO_REGISTRADO para cada inscrito.
 * 3. CERRAR asistencia al finalizar proyecto.
 * 4. Convertir NO_REGISTRADO → FALTA automáticamente.
 *
 * Se ejecuta diariamente a las 00:00.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProyectoAsistenciaScheduleService {

    private final ProyectoRepository proyectoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;

    /**
     * Tarea programada:
     * - Cron: todos los días a las 00:00
     * - Zona: America/Lima
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void procesarAsistencias() {

        LocalDate hoy = LocalDate.now();
        List<ProyectoEntity> proyectos = proyectoRepository.findAll();

        for (ProyectoEntity p : proyectos) {

            // ================================================
            // 1️⃣ ACTIVAR asistencia al iniciar el proyecto
            // ================================================
            if (hoy.isEqual(p.getFechaInicioProyecto()) && !p.getAsistenciaActiva()) {

                log.info("[AsistenciaScheduler] Activando asistencia para proyecto {}", p.getId());
                p.activarAsistencia();

                // Crear registros NO_REGISTRADO
                generarAsistenciasIniciales(p);

                continue;
            }

            // ================================================
            // 2️⃣ CERRAR asistencia al finalizar el proyecto
            // ================================================
            if (hoy.isAfter(p.getFechaFinProyecto()) && !p.getAsistenciaCerrada()) {

                log.info("[AsistenciaScheduler] Cerrando asistencia para proyecto {}", p.getId());
                p.cerrarAsistencia();

                // Convertir NO_REGISTRADO → FALTA
                marcarFaltas(p);

                continue;
            }
        }

        proyectoRepository.saveAll(proyectos);
    }

    // ============================================================
    // MÉTODOS DE APOYO
    // ============================================================

    /**
     * Crear una asistencia inicial por cada inscrito (NO_REGISTRADO).
     */
    private void generarAsistenciasIniciales(ProyectoEntity proyecto) {

        List<InscripcionEntity> inscripciones =
                inscripcionRepository.findByProyectoId(proyecto.getId());

        for (InscripcionEntity ins : inscripciones) {

            boolean yaExiste = asistenciaRepository
                    .findByProyectoIdAndUsuarioId(proyecto.getId(), ins.getUsuario().getId())
                    .isPresent();

            if (!yaExiste) {
                AsistenciaEntity asistencia = AsistenciaEntity.builder()
                        .proyecto(proyecto)
                        .usuario(ins.getUsuario())
                        .estado(AsistenciaEntity.EstadoAsistencia.NO_REGISTRADO)
                        .fechaRegistro(LocalDateTime.now())
                        .build();

                asistenciaRepository.save(asistencia);
            }
        }

        log.info("[AsistenciaScheduler] Registros NO_REGISTRADO creados para proyecto {}", proyecto.getId());
    }

    /**
     * Convertir todos los NO_REGISTRADO a FALTA al cerrar asistencia.
     */
    private void marcarFaltas(ProyectoEntity proyecto) {

        List<AsistenciaEntity> asistencias =
                asistenciaRepository.findByProyectoId(proyecto.getId());

        for (AsistenciaEntity a : asistencias) {
            if (a.getEstado() == AsistenciaEntity.EstadoAsistencia.NO_REGISTRADO) {
                a.setEstado(AsistenciaEntity.EstadoAsistencia.FALTA);
                a.setFechaRegistro(LocalDateTime.now());
                asistenciaRepository.save(a);
            }
        }

        log.info("[AsistenciaScheduler] Faltas aplicadas automáticamente en proyecto {}", proyecto.getId());
    }
}
