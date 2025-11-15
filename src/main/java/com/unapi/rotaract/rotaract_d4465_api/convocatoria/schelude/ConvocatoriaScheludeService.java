package com.unapi.rotaract.rotaract_d4465_api.convocatoria.schelude;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio encargado de ejecutar tareas programadas sobre convocatorias.
 *
 * Funcionalidad principal:
 * - Desactivar automáticamente las convocatorias cuya fechaFin ya venció.
 * - Rechazar las inscripciones en estado PENDIENTE asociadas a cada convocatoria desactivada.
 *
 * Características:
 * - Frecuencia: se ejecuta diariamente a medianoche (00:00) hora de Perú (America/Lima).
 * - Eficiencia: el rechazo de inscripciones se realiza mediante un UPDATE masivo controlado por el repositorio.
 * - Seguridad y consistencia: proceso transaccional para evitar estados parciales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocatoriaScheludeService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final InscripcionRepository inscripcionRepository;

    /**
     * Desactiva convocatorias vencidas y rechaza sus inscripciones pendientes.
     *
     * Flujo:
     * 1. Obtiene todas las convocatorias activas cuya fechaFin ya expiró.
     * 2. Cambia su atributo activo a false.
     * 3. Persiste los cambios.
     * 4. Ejecuta un UPDATE masivo para rechazar inscripciones pendientes.
     *
     * Cron:
     * 0 0 0 * * *  → todos los días a las 00:00:00 (medianoche).
     * Zona horaria explícita: America/Lima para asegurar ejecución correcta en Railway/UTC.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void deactivateExpiredConvocatorias() {

        List<ConvocatoriaEntity> convocatoriaEntities =
                convocatoriaRepository.findByActivoTrueAndFechaFinBefore(LocalDate.now());

        if (convocatoriaEntities.isEmpty()) {
            log.info("No hay convocatorias para desactivar");
            return;
        }

        // Desactivar convocatorias vencidas
        convocatoriaEntities.forEach(convocatoria -> convocatoria.setActivo(false));
        convocatoriaRepository.saveAll(convocatoriaEntities);

        // Rechazar inscripciones pendientes por cada convocatoria desactivada
        convocatoriaEntities.forEach(convocatoria -> {
            int rechazados = inscripcionRepository.rechazarPendientesPorConvocatoria(convocatoria.getId());
            log.info("Convocatoria {} desactivada: {} inscripciones pendientes rechazadas",
                    convocatoria.getId(), rechazados);
        });

        log.info("Total de convocatorias desactivadas: {}", convocatoriaEntities.size());
    }

}
