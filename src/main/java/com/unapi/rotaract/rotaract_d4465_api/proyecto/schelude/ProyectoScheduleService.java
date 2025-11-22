package com.unapi.rotaract.rotaract_d4465_api.proyecto.schelude;

import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity.EstadoProyecto;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler encargado de actualizar automáticamente el estado de los proyectos
 * de acuerdo con sus fechas de postulación y ejecución.
 *
 * Estados manejados:
 * - PROPUESTO: antes del inicio de postulación.
 * - EN_POSTULACION: dentro del rango de postulación.
 * - EN_EJECUCION: cuando el proyecto ya inició su ejecución.
 * - FINALIZADO: si terminó la fecha fin de ejecución.
 *
 * Los estados CANCELADO y FINALIZADO no se modifican.
 *
 * Este scheduler NO maneja asistencias todavía. Ese módulo se integrará
 * en el siguiente paso sin romper esta lógica.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProyectoScheduleService {

    private final ProyectoRepository proyectoRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void actualizarEstados() {

        LocalDate hoy = LocalDate.now();
        List<ProyectoEntity> proyectos = proyectoRepository.findAll();

        for (ProyectoEntity p : proyectos) {

            EstadoProyecto estadoActual = p.getEstadoProyecto();

            // No tocar proyectos ya cancelados o finalizados
            if (estadoActual == EstadoProyecto.CANCELADO ||
                    estadoActual == EstadoProyecto.FINALIZADO) {
                continue;
            }

            // 1. Antes de la postulación → PROPUESTO
            if (hoy.isBefore(p.getFechaInicioPostulacion())) {
                p.setEstadoProyecto(EstadoProyecto.PROPUESTO);
                continue;
            }

            // 2. Dentro del periodo de postulación → EN_POSTULACION
            if (!hoy.isAfter(p.getFechaFinPostulacion())) {
                p.setEstadoProyecto(EstadoProyecto.EN_POSTULACION);
                continue;
            }

            // 3. Periodo de ejecución → EN_EJECUCION
            if (!hoy.isBefore(p.getFechaInicioProyecto()) &&
                    !hoy.isAfter(p.getFechaFinProyecto())) {
                p.setEstadoProyecto(EstadoProyecto.EN_EJECUCION);
                continue;
            }

            // 4. Proyecto terminado → FINALIZADO
            if (hoy.isAfter(p.getFechaFinProyecto())) {
                p.setEstadoProyecto(EstadoProyecto.FINALIZADO);
                continue;
            }
        }

        proyectoRepository.saveAll(proyectos);
        log.info("Scheduler: estados de proyectos actualizados correctamente.");
    }
}
