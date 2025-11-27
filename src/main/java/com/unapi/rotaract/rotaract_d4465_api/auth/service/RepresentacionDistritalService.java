package com.unapi.rotaract.rotaract_d4465_api.auth.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.interfaces.IRepresentacionDistritalService;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
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

    @Override
    public void transferirRepresentacionDistrital(Long nuevoId) {

        // Usuario autenticado (quien transfiere)
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity actualRD = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        // Validar que sea representante distrital
        if (!actualRD.getRol().getNombre().equals("REPRESENTANTE DISTRITAL")) {
            throw new IllegalArgumentException("Solo el representante distrital puede realizar esta acción.");
        }

        // Nuevo representante
        UsuarioEntity nuevo = usuarioRepository.findById(nuevoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario destino no encontrado."));

        // No puede ser presidente
        if (nuevo.getRol().getNombre().equals("PRESIDENTE")) {
            throw new IllegalArgumentException("Debe transferir su presidencia antes de asumir la representación distrital.");
        }

        // Solo SOCIO o INTERESADO
        boolean esElegible =
                nuevo.getRol().getNombre().equals("SOCIO") ||
                        nuevo.getRol().getNombre().equals("INTERESADO");

        if (!esElegible) {
            throw new IllegalArgumentException("Solo un SOCIO o un INTERESADO puede ser representante distrital.");
        }

        RolEntity rolRD = rolRepository.findByNombre("REPRESENTANTE DISTRITAL")
                .orElseThrow(() -> new IllegalStateException("Rol REPRESENTANTE DISTRITAL no encontrado."));

        RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                .orElseThrow(() -> new IllegalStateException("Rol SOCIO no encontrado."));

        // Transferir
        actualRD.setRol(rolSocio);
        nuevo.setRol(rolRD);

        usuarioRepository.save(actualRD);
        usuarioRepository.save(nuevo);

        // --------------------------------------------------------------------
        // 🔥 ENVÍO DE CORREOS
        // --------------------------------------------------------------------

        enviarCorreoNuevoRepresentante(nuevo, actualRD);
        enviarCorreoAntiguoRepresentante(actualRD, nuevo);
    }


    // ------------------------------
    // 📧 Correo para el nuevo RD
    // ------------------------------
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
            <p>¡Éxitos en tu gestión!</p>
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

    // ------------------------------
    // 📧 Correo para el RD saliente
    // ------------------------------
    private void enviarCorreoAntiguoRepresentante(UsuarioEntity anterior, UsuarioEntity nuevo) {

        String asunto = "Transferencia de Representación Distrital realizada - Rotaract D4465";

        String html = """
            <h2>Hola %s</h2>
            <p>Confirmamos que has transferido la <strong>Representación Distrital</strong> a:</p>
            <p><strong>%s</strong></p>
            <br>
            <p>Tu rol ha sido actualizado nuevamente a <strong>SOCIO</strong>.</p>
            <p>Gracias por tu involucramiento y servicio durante tu gestión distrital.</p>
            <br>
            <p>Siempre serás parte importante de Rotaract D4465.</p>
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
