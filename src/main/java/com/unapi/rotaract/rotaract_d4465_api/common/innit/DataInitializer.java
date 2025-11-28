package com.unapi.rotaract.rotaract_d4465_api.common.innit;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Inicializador de datos base del sistema.
 *
 * SOLO realiza:
 *  - Creación de roles del sistema (INTERESADO, SOCIO, PRESIDENTE, REPRESENTANTE DISTRITAL)
 *  - Creación del Representante Distrital por defecto
 *
 * NO genera clubes ni usuarios adicionales.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        inicializarRoles();
        inicializarRepresentanteDistrital();
    }

    // ============================================================
    // 1. CREACIÓN DE ROLES BASE
    // ============================================================

    private void inicializarRoles() {

        List<String> rolesBase = List.of(
                "INTERESADO",
                "SOCIO",
                "PRESIDENTE",
                "REPRESENTANTE DISTRITAL"
        );

        rolesBase.forEach(nombreRol ->
                rolRepository.findByNombre(nombreRol)
                        .orElseGet(() -> {
                            RolEntity nuevoRol = new RolEntity();
                            nuevoRol.setNombre(nombreRol);
                            RolEntity guardado = rolRepository.save(nuevoRol);

                            System.out.println("Rol creado: " + guardado.getNombre());
                            return guardado;
                        })
        );

        System.out.println("Inicialización de roles completada correctamente.");
    }

    // ============================================================
    // 2. CREACIÓN DEL REPRESENTANTE DISTRITAL
    // ============================================================

    private void inicializarRepresentanteDistrital() {

        Optional<UsuarioEntity> existente =
                usuarioRepository.findByCorreo("joanpsolignac@gmail.com");

        RolEntity rolRD = rolRepository.findByNombre("REPRESENTANTE DISTRITAL")
                .orElseThrow(() -> new RuntimeException("Rol REPRESENTANTE DISTRITAL no encontrado"));

        if (existente.isEmpty()) {

            UsuarioEntity representante = new UsuarioEntity();
            representante.setNombre("JOAN PIERO SOLIGNAC LOVERA");
            representante.setCorreo("joanpsolignac@gmail.com");
            representante.setContrasena(passwordEncoder.encode("1234567890"));
            representante.setFechaNacimiento(LocalDate.of(2003, 3, 8));
            representante.setRol(rolRD);
            representante.setActivo(true);
            representante.setClub(null);  // Un RD NO tiene club

            usuarioRepository.save(representante);

            System.out.println("✅ Representante Distrital creado correctamente.");

        } else {
            System.out.println("ℹ️ El Representante Distrital ya existe, no se creó nuevamente.");
        }
    }
}
