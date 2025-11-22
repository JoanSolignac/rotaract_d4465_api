package com.unapi.rotaract.rotaract_d4465_api.convocatoria.schelude;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio encargado de administrar automáticamente el ciclo de vida
 * de las convocatorias según sus fechas de cierre.
 *
 * Todas las convocatorias cuyo estado es ACTIVO y cuya fecha de cierre ya pasó,
 * serán marcadas como CERRADO.
 *
 * La tarea se ejecuta:
 * - Una vez al día a las 00:00 (medianoche)
 * - Zona horaria: America/Lima
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocatoriaScheduleService {

    private final ConvocatoriaRepository convocatoriaRepository;

    /**
     * Tarea programada que desactiva automáticamente convocatorias vencidas.
     *
     * Reglas:
     * - Solo se toman convocatorias con estado ACTIVO.
     * - Si la fecha de cierre es antes de la fecha actual, se marcan como CERRADO.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void cerrarConvocatoriasVencidas() {

        LocalDate hoy = LocalDate.now();

        // 1. Obtener todas las convocatorias activas
        List<ConvocatoriaEntity> activas =
                convocatoriaRepository.findByEstado(EventoEntity.EstadoEvento.ACTIVO);

        if (activas.isEmpty()) {
            log.info("[ConvocatoriaScheduler] No hay convocatorias activas para revisar.");
            return;
        }

        // 2. Filtrar convocatorias cuyo periodo de postulación ya terminó
        List<ConvocatoriaEntity> vencidas = activas.stream()
                .filter(c -> c.getFechaCierre().isBefore(hoy))
                .toList();

        if (vencidas.isEmpty()) {
            log.info("[ConvocatoriaScheduler] No hay convocatorias vencidas para cerrar en esta ejecución.");
            return;
        }

        // 3. Marcar como cerradas
        vencidas.forEach(c -> c.setEstado(EventoEntity.EstadoEvento.CERRADO));
        convocatoriaRepository.saveAll(vencidas);

        log.info("[ConvocatoriaScheduler] Convocatorias cerradas automáticamente: {}", vencidas.size());
    }
}
