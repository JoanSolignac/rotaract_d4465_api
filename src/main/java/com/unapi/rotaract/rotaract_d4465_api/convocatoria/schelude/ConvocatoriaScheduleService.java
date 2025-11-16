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
 * Servicio responsable de ejecutar la tarea programada que administra el ciclo
 * de vida de las convocatorias. Evalúa diariamente la vigencia de las mismas
 * según su fecha de cierre y actualiza su estado a INACTIVO cuando corresponde.
 *
 * Características:
 * - Frecuencia diaria a medianoche.
 * - Procesamiento transaccional para evitar estados parciales.
 * - Basado únicamente en el atributo "estado" definido en {@link EventoEntity}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocatoriaScheduleService {

    private final ConvocatoriaRepository convocatoriaRepository;

    /**
     * Desactiva convocatorias vencidas mediante la actualización de su estado
     * a INACTIVO. El proceso identifica convocatorias cuyo estado actual es ACTIVO
     * y cuya fecha de cierre ya fue superada por la fecha del sistema.
     *
     * Cron:
     * - Expresión: 0 0 0 * * * (ejecución diaria a las 00:00).
     * - Zona horaria: America/Lima.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void deactivateExpiredConvocatorias() {

        LocalDate today = LocalDate.now();

        // Recupera todas las convocatorias activas
        List<ConvocatoriaEntity> activas =
                convocatoriaRepository.findByEstado(EventoEntity.EstadoEvento.ACTIVO);

        // Filtra las vencidas
        List<ConvocatoriaEntity> vencidas = activas.stream()
                .filter(c -> c.getFechaCierre().isBefore(today))
                .toList();

        if (vencidas.isEmpty()) {
            log.info("No se encontraron convocatorias vencidas para desactivar.");
            return;
        }

        vencidas.forEach(c -> c.setEstado(EventoEntity.EstadoEvento.CERRADO));
        convocatoriaRepository.saveAll(vencidas);

        log.info("Convocatorias desactivadas por vencimiento: {}", vencidas.size());
    }
}
