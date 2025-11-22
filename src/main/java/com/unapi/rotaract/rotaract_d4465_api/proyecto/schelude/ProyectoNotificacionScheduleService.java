package com.unapi.rotaract.rotaract_d4465_api.proyecto.schelude;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProyectoNotificacionScheduleService {

    private final ProyectoRepository proyectoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final IEmailService emailService;

    /**
     * ENVÍA CORREO 1 DÍA ANTES DEL INICIO DEL PROYECTO
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Lima")
    public void notificarInicioProyectos() {

        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);

        // Buscar proyectos que inician mañana
        List<ProyectoEntity> proyectos = proyectoRepository
                .findByFechaInicioProyecto(manana);

        if (proyectos.isEmpty()) {
            log.info("[Scheduler Inicio Proyecto] No hay proyectos que inicien mañana.");
            return;
        }

        for (ProyectoEntity proyecto : proyectos) {

            // Obtener inscritos aceptados
            List<InscripcionEntity> aceptados = inscripcionRepository
                    .findByProyectoId(proyecto.getId())
                    .stream()
                    .filter(i -> i.getEstado() == InscripcionEntity.EstadoInscripcion.ACEPTADA)
                    .toList();

            // 1. Notificar a cada inscrito aceptado
            for (InscripcionEntity insc : aceptados) {

                UsuarioEntity usuario = insc.getUsuario();

                emailService.enviarCorreo(
                        usuario.getCorreo(),
                        "Recordatorio: Proyecto inicia mañana - " + proyecto.getTitulo(),
                        "<h1>Hola, " + usuario.getNombre() + "</h1>"
                                + "<p>Este es un recordatorio de que el proyecto:</p>"
                                + "<h2>" + proyecto.getTitulo() + "</h2>"
                                + "<p>Inicia mañana.</p>"
                                + "<p>Organizado por el club <b>" + proyecto.getClub().getNombre() + "</b>.</p>"
                                + "<p>Te esperamos.</p>"
                );
            }

            // 2. Notificar al presidente del club
            proyecto.getClub().getMiembros().stream()
                    .filter(m -> m.getRol().getNombre().equalsIgnoreCase("PRESIDENTE"))
                    .findFirst()
                    .ifPresent(presidente -> emailService.enviarCorreo(
                            presidente.getCorreo(),
                            "Recordatorio: Tu proyecto inicia mañana",
                            "<h1>Recordatorio de Proyecto</h1>"
                                    + "<p>El proyecto:</p>"
                                    + "<h2>" + proyecto.getTitulo() + "</h2>"
                                    + "<p>Inicia mañana.</p>"
                    ));
        }

        log.info("[Scheduler Inicio Proyecto] Correos enviados correctamente.");
    }
}
