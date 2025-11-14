package com.unapi.rotaract.rotaract_d4465_api.convocatoria.schelude;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de programación de tareas sobre convocatorias.
 *
 * Función principal: revisar diariamente las convocatorias activas cuya fechaFin ya expiró
 * y marcarlas como inactivas (activo = false) para que no sigan apareciendo como vigentes.
 *
 * Características:
 * - Frecuencia: se ejecuta a medianoche usando la expresión cron 0 0 0 * * * (todos los días).
 * - Estrategia actual: carga la lista de convocatorias vencidas y actualiza su flag en memoria,
 *   luego realiza un saveAll.
 * - Logging: registra cuántas fueron desactivadas o si no hubo cambios.
 *
 * Posibles mejoras futuras (no implementadas aquí porque sólo se documenta):
 * - Reemplazar el bucle por un UPDATE masivo en el repositorio para mayor eficiencia.
 * - Añadir métricas (Micrometer) para monitoreo de cantidades y tiempos.
 * - Parametrizar zona horaria con el atributo zone de @Scheduled si el servidor no está en la TZ deseada.
 * - Control de concurrencia si se habilitan múltiples nodos (usar locks o shedlock).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocatoriaScheludeService {

    private final ConvocatoriaRepository convocatoriaRepository;

    /**
     * Desactiva convocatorias cuya fechaFin es anterior a la fecha actual y aún están marcadas como activas.
     *
     * Flujo:
     * 1. Obtiene todas las convocatorias activas vencidas usando el repositorio.
     * 2. Cambia su atributo activo a false una por una.
     * 3. Persiste los cambios con saveAll.
     * 4. Registra en logs la cantidad afectada o que no hay registros para desactivar.
     *
     * Cron: 0 0 0 * * * (todos los días a las 00:00:00). Si se requiere otra zona horaria se puede agregar
     * el parámetro zone en la anotación @Scheduled.
     *
     * Nota de rendimiento: Para un volumen grande, podría optarse por una sentencia UPDATE masiva para evitar
     * cargar todas las entidades en memoria.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void deactivateExpiredConvocatorias() {

        List<ConvocatoriaEntity> convocatoriaEntities = convocatoriaRepository.findByActivoTrueAndFechaFinBefore(LocalDate.now());

        if (!convocatoriaEntities.isEmpty()) {
            convocatoriaEntities.forEach(
                    convocatoriaEntity -> {
                        convocatoriaEntity.setActivo(false);
                    }
            );
            convocatoriaRepository.saveAll(convocatoriaEntities);
            log.info("Cantidad de convocatorias desactivadas: {}", convocatoriaEntities.size());
        }else{
            log.info("No hay convocatorias para desactivar");
        }
    }

}
