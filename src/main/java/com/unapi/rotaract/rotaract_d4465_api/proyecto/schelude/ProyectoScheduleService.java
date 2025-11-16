package com.unapi.rotaract.rotaract_d4465_api.proyecto.schedule;

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
 * Servicio encargado de gestionar la actualización automática del estado
 * de los proyectos según las fechas registradas en su ciclo de vida.
 *
 * Este componente evalúa diariamente la fase en la que se encuentra cada
 * proyecto y ajusta su estado conforme a las reglas definidas:
 * - PROPUESTO: etapa previa al inicio de postulaciones.
 * - EN_POSTULACION: mientras la fecha actual se encuentre dentro del periodo de postulación.
 * - EN_EJECUCION: cuando ha iniciado la ejecución del proyecto.
 * - FINALIZADO: al concluir la fecha final de ejecución.
 *
 * Los proyectos en estado CANCELADO o FINALIZADO no son modificados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProyectoScheduleService {

    private final ProyectoRepository proyectoRepository;

    /**
     * Analiza y actualiza el estado de todos los proyectos del sistema.
     * Se ejecuta diariamente a medianoche utilizando la zona horaria
     * America/Lima para mantener consistencia con la operación regional.
     *
     * Proceso:
     * 1. Recupera todos los proyectos registrados.
     * 2. Evalúa la fecha actual respecto a sus fechas de postulación
     *    y ejecución.
     * 3. Actualiza el estado según corresponda.
     * 4. Omite proyectos ya cancelados o finalizados.
     * 5. Persiste los cambios de manera transaccional.
     *
     * Cron: 0 0 0 * * *  — ejecución diaria a las 00:00.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void actualizarEstados() {

        LocalDate hoy = LocalDate.now();

        List<ProyectoEntity> proyectos = proyectoRepository.findAll();

        for (ProyectoEntity p : proyectos) {

            if (p.getEstadoProyecto() == EstadoProyecto.CANCELADO ||
                    p.getEstadoProyecto() == EstadoProyecto.FINALIZADO)
                continue;

            if (hoy.isBefore(p.getFechaInicioPostulacion()))
                p.setEstadoProyecto(EstadoProyecto.PROPUESTO);

            else if (!hoy.isAfter(p.getFechaFinPostulacion()))
                p.setEstadoProyecto(EstadoProyecto.EN_POSTULACION);

            else if (!hoy.isBefore(p.getFechaInicioProyecto()))
                p.setEstadoProyecto(EstadoProyecto.EN_EJECUCION);

            if (hoy.isAfter(p.getFechaFinProyecto()))
                p.setEstadoProyecto(EstadoProyecto.FINALIZADO);
        }

        proyectoRepository.saveAll(proyectos);

        log.info("Scheduler: estados de proyectos actualizados correctamente.");
    }
}
