package com.unapi.rotaract.rotaract_d4465_api.asistencia.schelude;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduler encargado de enviar:
 *
 * 1. Recordatorio 24 horas antes del inicio del proyecto
 *    - Participantes aceptados
 *    - Presidente del club
 *
 * 2. Recordatorio 24 horas antes del fin del proyecto (cierre asistencia)
 *    - Presidente del club
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenciaNotificacionScheduleService {

    private final ProyectoRepository proyectoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final IEmailService emailService;

    /**
     * Tarea programada:
     * - Todos los días a las 00:00
     * - Zona: America/Lima
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void enviarRecordatorios() {

        LocalDate hoy = LocalDate.now();
        List<ProyectoEntity> proyectos = proyectoRepository.findAll();

        if (proyectos.isEmpty()) {
            log.info("[Notificaciones Asistencia] No hay proyectos para procesar.");
            return;
        }

        for (ProyectoEntity proyecto : proyectos) {
            notificarInicioProyecto(proyecto, hoy);
            notificarCierreAsistencia(proyecto, hoy);
        }
    }

    // ===============================================================
    // 1️⃣ RECORDATORIO — INICIO DEL PROYECTO
    // ===============================================================

    private void notificarInicioProyecto(ProyectoEntity proyecto, LocalDate hoy) {

        if (proyecto.getFechaInicioProyecto() == null) return;

        if (!hoy.isEqual(proyecto.getFechaInicioProyecto().minusDays(1))) return;

        // Participantes aceptados
        List<UsuarioEntity> aceptados = inscripcionRepository.findByProyectoId(proyecto.getId())
                .stream()
                .filter(ins -> ins.getEstado() == InscripcionEntity.EstadoInscripcion.ACEPTADA)
                .map(InscripcionEntity::getUsuario)
                .collect(Collectors.toList());

        // Presidente
        UsuarioEntity presidente = proyecto.getClub()
                .getMiembros()
                .stream()
                .filter(m -> "PRESIDENTE".equalsIgnoreCase(m.getRol().getNombre()))
                .findFirst()
                .orElse(null);

        // Correos a participantes aceptados
        for (UsuarioEntity u : aceptados) {
            emailService.enviarCorreo(
                    u.getCorreo(),
                    "Recordatorio: el proyecto \"" + proyecto.getTitulo() + "\" inicia mañana",
                    "<h1>¡Hola, " + u.getNombre() + "!</h1>" +
                            "<p>Este es un recordatorio de que mañana inicia el proyecto:</p>" +
                            "<h2>" + proyecto.getTitulo() + "</h2>" +
                            "<p>Organizado por el club <b>" + proyecto.getClub().getNombre() + "</b>.</p>"
            );
        }

        // Correo al presidente
        if (presidente != null) {
            emailService.enviarCorreo(
                    presidente.getCorreo(),
                    "Tu proyecto \"" + proyecto.getTitulo() + "\" inicia mañana",
                    "<h1>Recordatorio de proyecto</h1>" +
                            "<p>Mañana inicia el proyecto:</p>" +
                            "<h2>" + proyecto.getTitulo() + "</h2>" +
                            "<p>Total de aceptados: <b>" + aceptados.size() + "</b></p>"
            );
        }

        log.info("[Notificaciones Asistencia] Recordatorio de inicio enviado para proyecto {}", proyecto.getId());
    }

    // ===============================================================
    // 2️⃣ RECORDATORIO — CIERRE DE ASISTENCIA
    // ===============================================================

    private void notificarCierreAsistencia(ProyectoEntity proyecto, LocalDate hoy) {

        if (proyecto.getFechaFinProyecto() == null) return;

        // Solo si hoy = 1 día antes del fin
        if (!hoy.isEqual(proyecto.getFechaFinProyecto().minusDays(1))) return;

        // Solo si asistencia está activa
        if (!Boolean.TRUE.equals(proyecto.getAsistenciaActiva())
                || Boolean.TRUE.equals(proyecto.getAsistenciaCerrada())) return;

        // Presidente
        UsuarioEntity presidente = proyecto.getClub()
                .getMiembros()
                .stream()
                .filter(m -> "PRESIDENTE".equalsIgnoreCase(m.getRol().getNombre()))
                .findFirst()
                .orElse(null);

        if (presidente == null) return;

        emailService.enviarCorreo(
                presidente.getCorreo(),
                "Recordatorio: cierre de asistencia mañana - " + proyecto.getTitulo(),
                "<h1>Cierre de asistencia</h1>" +
                        "<p>Mañana es el último día del proyecto:</p>" +
                        "<h2>" + proyecto.getTitulo() + "</h2>" +
                        "<p>Debe completar y cerrar la asistencia antes de finalizar el día.</p>"
        );

        log.info("[Notificaciones Asistencia] Recordatorio de cierre enviado para proyecto {}", proyecto.getId());
    }
}
