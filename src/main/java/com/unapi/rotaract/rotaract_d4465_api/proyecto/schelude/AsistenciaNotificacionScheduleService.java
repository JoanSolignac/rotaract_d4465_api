package com.unapi.rotaract.rotaract_d4465_api.proyecto.schelude;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenciaNotificacionScheduleService {

    private final ProyectoRepository proyectoRepository;
    private final IEmailService emailService;

    /**
     * ENVÍA CORREO 1 DÍA ANTES DEL CIERRE DE ASISTENCIA
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void notificarCierreAsistencia() {

        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);

        // Buscar proyectos que mañana concluyen
        List<ProyectoEntity> proyectos = proyectoRepository
                .findByFechaFinProyecto(manana);

        if (proyectos.isEmpty()) {
            log.info("[Scheduler Cierre Asistencia] No hay proyectos que finalicen mañana.");
            return;
        }

        for (ProyectoEntity proyecto : proyectos) {

            // Buscar al presidente del club
            UsuarioEntity presidente = proyecto.getClub()
                    .getMiembros()
                    .stream()
                    .filter(m -> m.getRol().getNombre().equalsIgnoreCase("PRESIDENTE"))
                    .findFirst()
                    .orElse(null);

            if (presidente == null) {
                log.warn("[Scheduler Cierre Asistencia] No se encontró presidente para el club '{}'",
                        proyecto.getClub().getNombre());
                continue;
            }

            // Enviar correo
            emailService.enviarCorreo(
                    presidente.getCorreo(),
                    "Recordatorio: Cierre de asistencia mañana - " + proyecto.getTitulo(),
                    "<h1>Recordatorio de cierre de asistencia</h1>"
                            + "<p>El proyecto:</p>"
                            + "<h2>" + proyecto.getTitulo() + "</h2>"
                            + "<p>Concluye mañana.</p>"
                            + "<p>Debe cerrar la asistencia antes de finalizar el día.</p>"
            );

            log.info("[Scheduler Cierre Asistencia] Notificación enviada al presidente del club {}.",
                    proyecto.getClub().getNombre());
        }
    }
}
