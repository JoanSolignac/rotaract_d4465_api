package com.unapi.rotaract.rotaract_d4465_api.auth.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.interfaces.IRepresentacionDistritalService;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.NotificacionDto;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;



import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepresentacionDistritalService implements IRepresentacionDistritalService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final IEmailService emailService;
    private final com.unapi.rotaract.rotaract_d4465_api.common.services.NotificacionService notificacionService;

    @Override
    public void transferirRepresentacionDistrital(Long nuevoId) {

        // Usuario autenticado (quien transfiere)
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity actualRD = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        // Validar que sea representante distrital
        if (!"REPRESENTANTE DISTRITAL".equals(actualRD.getRol().getNombre())) {
            throw new IllegalArgumentException("Solo el representante distrital puede realizar esta acción.");
        }

        // Nuevo representante
        UsuarioEntity nuevo = usuarioRepository.findById(nuevoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario destino no encontrado."));

        // No puede ser presidente
        if ("PRESIDENTE".equals(nuevo.getRol().getNombre())) {
            throw new IllegalArgumentException("Debe transferir su presidencia antes de asumir la representación distrital.");
        }

        // Solo SOCIO o INTERESADO
        boolean esElegible =
                "SOCIO".equals(nuevo.getRol().getNombre()) ||
                        "INTERESADO".equals(nuevo.getRol().getNombre());

        if (!esElegible) {
            throw new IllegalArgumentException("Solo un SOCIO o un INTERESADO puede ser representante distrital.");
        }

        RolEntity rolRD = rolRepository.findByNombre("REPRESENTANTE DISTRITAL")
                .orElseThrow(() -> new IllegalStateException("Rol REPRESENTANTE DISTRITAL no encontrado."));

        RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                .orElseThrow(() -> new IllegalStateException("Rol SOCIO no encontrado."));

        // Transferir rol
        actualRD.setRol(rolSocio);
        nuevo.setRol(rolRD);

        // Invalidar sesiones previas: incrementar tokenVersion en ambos
        actualRD.setTokenVersion(actualRD.getTokenVersion() + 1);
        nuevo.setTokenVersion(nuevo.getTokenVersion() + 1);

        usuarioRepository.save(actualRD);
        usuarioRepository.save(nuevo);

        // Notificaciones en tiempo real via WebSocket
        enviarNotificacionesWebsocket(actualRD, nuevo);

        // Notificaciones por correo
        enviarCorreoNuevoRepresentante(nuevo, actualRD);
        enviarCorreoAntiguoRepresentante(actualRD, nuevo);
    }

    /**
     * Enviar notificaciones WebSocket al nuevo y al antiguo representante.
     */
    private void enviarNotificacionesWebsocket(UsuarioEntity anterior, UsuarioEntity nuevo) {

        // Nuevo representante
        notificacionService.enviarAUsuario(
                nuevo.getId(),
                new NotificacionDto(
                        "Designación como Representante Distrital",
                        "Ha sido designado como representante distrital del Distrito Rotaract 4465. " +
                                "La representación fue transferida por " + anterior.getNombre() + "."
                )
        );

        // Representante saliente
        notificacionService.enviarAUsuario(
                anterior.getId(),
                new NotificacionDto(
                        "Transferencia de representación distrital realizada",
                        "La representación distrital ha sido transferida a " + nuevo.getNombre() + ". " +
                                "Su rol ha sido actualizado a SOCIO."
                )
        );
    }

    /**
     * Enviar correo al nuevo representante distrital.
     */
    private void enviarCorreoNuevoRepresentante(UsuarioEntity nuevo, UsuarioEntity anterior) {

        String asunto = "Has sido designado como Representante Distrital - Rotaract D4465";

        String html = """
            <h2>Felicitaciones, %s</h2>
            <p>Has sido oficialmente designado como <strong>Representante Distrital del Distrito Rotaract 4465</strong>.</p>
            <p>Esta asignación fue realizada por %s.</p>
            <br>
            <p>Desde este momento, tienes acceso al panel distrital para gestionar:</p>
            <ul>
                <li>Clubes del distrito</li>
                <li>Activación y desactivación de clubes</li>
                <li>Creación de nuevos clubes</li>
                <li>Métricas distritales</li>
            </ul>
            <br>
            <p>Éxitos en su gestión.</p>
            """.formatted(
                nuevo.getNombre(),
                anterior.getNombre()
        );

        emailService.enviarCorreo(
                nuevo.getCorreo(),
                asunto,
                html
        );
    }

    /**
     * Enviar correo al representante distrital saliente.
     */
    private void enviarCorreoAntiguoRepresentante(UsuarioEntity anterior, UsuarioEntity nuevo) {

        String asunto = "Transferencia de Representación Distrital realizada - Rotaract D4465";

        String html = """
            <h2>Hola %s</h2>
            <p>Confirmamos que ha transferido la <strong>Representación Distrital</strong> a:</p>
            <p><strong>%s</strong></p>
            <br>
            <p>Su rol ha sido actualizado nuevamente a <strong>SOCIO</strong>.</p>
            <p>Agradecemos su compromiso y servicio durante su gestión distrital.</p>
            <br>
            <p>Siempre será parte importante de Rotaract D4465.</p>
            """.formatted(
                anterior.getNombre(),
                nuevo.getNombre()
        );

        emailService.enviarCorreo(
                anterior.getCorreo(),
                asunto,
                html
        );
    }
}
