package com.unapi.rotaract.rotaract_d4465_api.common.innit;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;

import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Inicializador de datos para el ambiente local / desarrollo.
 * Este componente se ejecuta al arrancar la aplicación (implementa {@link CommandLineRunner})
 * y se encarga de crear datos mínimos necesarios en la base de datos, como roles y clubes.
 * Notas importantes:
 * - Por seguridad y para evitar inserciones accidentales en entornos de producción,
 *   la inicialización de usuarios (método {@link #inicializarUsuarios()}) no se llama
 *   por defecto desde {@link #run(String...) run}. Si deseas crear usuarios de ejemplo,
 *   habilita la llamada a {@code inicializarUsuarios()} manualmente desde {@code run}.
 * - El comportamiento actual crea roles base y un club de ejemplo "LAZOS AMAZONICOS"
 *   solo si no existen previamente.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClubRepository clubRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Método que se ejecuta al inicio de la aplicación.
     * Actualmente ejecuta:
     *  - {@link #inicializarRoles()} : asegura la presencia de roles base.
     *  - {@link #inicializarClubes()} : asegura la presencia de un club de ejemplo.
     * Para evitar creación automática de usuarios de ejemplo, la llamada a
     * {@link #inicializarUsuarios()} está deshabilitada por defecto; habilítala
     * manualmente aquí si necesitas poblar usuarios en desarrollo.
     *
     * @param args argumentos de línea de comando (no usados)
     */
    @Override
    public void run(String... args) {
        inicializarRoles();
        inicializarClubes();
        inicializarUsuarios();
    }

    /**
     * Crea los roles base del sistema si no existen.
     * Roles añadidos: INTERESADO, INVITADO, SOCIO, PRESIDENTE, REPRESENTANTE DISTRITAL.
     * Cada nombre se busca en la BD y se crea solo si está ausente.
     */
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

    /**
     * Crea clubes de ejemplo si no existen.
     * Actualmente crea un club llamado "LAZOS AMAZONICOS" localizado en
     * Loreto / Iquitos. Si ya existe, no se realizará ninguna acción.
     */
    private void inicializarClubes(){
        Optional<ClubEntity> existente = clubRepository.findByNombre("LAZOS AMAZONICOS");
        if (existente.isEmpty()) {
            ClubEntity lazosAmazonicos = new ClubEntity();
            lazosAmazonicos.setNombre("LAZOS AMAZONICOS");
            lazosAmazonicos.setDepartamento("LORETO");
            lazosAmazonicos.setCiudad("IQUITOS");
            lazosAmazonicos.setFechaCreacion(LocalDate.now());
            lazosAmazonicos.setActivo(true);

            clubRepository.saveAll(List.of(lazosAmazonicos));
            System.out.println("Clubes creados correctamente.");
        } else {
            System.out.println("Los clubes ya existe, no se crearon nuevamente.");
        }
    }

    /**
     * Crea usuarios de ejemplo vinculados a roles y clubes existentes.
     * Este método está separado y no se ejecuta por defecto desde {@link #run(String...)}
     * para evitar inserciones accidentales. Contiene lógica para:
     *  - Crear un representante distrital si no existe.
     *  - Crear un usuario "Giancarlo" vinculado al club "LAZOS AMAZONICOS" si no existe.
     * Las contraseñas se codifican usando el {@link PasswordEncoder} inyectado.
     */
    @SuppressWarnings("unused")
    private void inicializarUsuarios(){
        // 1. Crear el rol REPRESENTANTE DISTRITAL si no existe
        RolEntity rolRepresentante = rolRepository.findByNombre("REPRESENTANTE DISTRITAL")
                .orElseGet(() -> {
                    RolEntity nuevoRol = new RolEntity();
                    nuevoRol.setNombre("REPRESENTANTE");
                    return rolRepository.save(nuevoRol);
                });

        // 2. Crear usuario principal del representante distrital
        Optional<UsuarioEntity> existente = usuarioRepository.findByCorreo("representantedistrital@rotaract.org");

        if (existente.isEmpty()) {
            UsuarioEntity representante = new UsuarioEntity();
            representante.setNombre("Representante Distrital");
            representante.setCorreo("representantedistrital@rotaract.org");
            representante.setContrasena(passwordEncoder.encode("1234567890"));
            representante.setFechaNacimiento(LocalDate.of(1995, 1, 1));
            representante.setRol(rolRepresentante);
            representante.setActivo(true);

            usuarioRepository.save(representante);
            System.out.println("✅ Usuario Representante Distrital creado correctamente.");
        } else {
            System.out.println("ℹ️ El usuario Representante Distrital ya existe, no se volvió a crear.");
        }

        RolEntity rolSocio = rolRepository.findByNombre("PRESIDENTE")
                .orElseGet(() -> {
                    RolEntity nuevoRol = new RolEntity();
                    nuevoRol.setNombre("SOCIO");
                    return rolRepository.save(nuevoRol);
                });

        Optional<UsuarioEntity> existenteG = usuarioRepository.findByCorreo("giancarlochavez@gmail.com");
        Optional<ClubEntity> existenteC = clubRepository.findByNombre("LAZOS AMAZONICOS");

        if (existenteG.isEmpty() && existenteC.isPresent()) {
            UsuarioEntity giancarlo = new UsuarioEntity();
            giancarlo.setNombre("GIANCARLO ANDRE CHAVEZ PEZO");
            giancarlo.setCorreo("giancarlochavez@gmail.com");
            giancarlo.setContrasena(passwordEncoder.encode("1234567890"));
            giancarlo.setFechaNacimiento(LocalDate.of(1998, 1, 1)); // de ejemplo
            giancarlo.setRol(rolSocio);
            giancarlo.setActivo(true);
            giancarlo.setClub(existenteC.get());
            usuarioRepository.save(giancarlo);
            System.out.println("Usuario Giancarlo Andre Chavez Pezo creado correctamente.");
        } else {
            System.out.println("El usuario Giancarlo Andre Chavez Pezo ya existe, no se creó nuevamente.");
        }
    }
}
