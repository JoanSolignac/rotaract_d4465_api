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

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClubRepository clubRepository;

    @Override
    public void run(String... args) throws Exception {
        inicializarClub();
        inicializarRoles();
        inicializarPresidente();
    }

    private void inicializarRoles() {
        List<String> rolesBase = List.of(
                "INTERESADO",
                "INVITADO",
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

    private void inicializarClub() {
        String nombreClub = "LAZOS AMAZONICOS";

        // Si ya existe, no hacer nada
        if (clubRepository.findByNombre(nombreClub).isPresent()) {
            System.out.println("Club base ya existe: " + nombreClub);
            return;
        }

        // Crear club base
        ClubEntity club = ClubEntity.builder()
                .nombre(nombreClub)
                .departamento("LORETO")
                .ciudad("IQUITOS")
                .activo(true)
                .build();

        clubRepository.save(club);

        System.out.println("✅ Club base creado: " + nombreClub);
    }

    private void inicializarPresidente()
    {
        String correoPresidente = "giancarlochavez@gmail.com";

        // Si ya existe, no hacer nada
        if (usuarioRepository.findByCorreo(correoPresidente).isPresent()) {
            System.out.println("Presidente base ya existe: " + correoPresidente);
            return;
        }

        //Obtener el club del presidente
        ClubEntity club = clubRepository.findByNombre("LAZOS AMAZONICOS")
                .orElseThrow(() -> new RuntimeException("El club LAZOS AMAZONICOS no está registrado."));

        // Obtener el rol PRESIDENTE
        RolEntity rolPresidente = rolRepository.findByNombre("PRESIDENTE")
                .orElseThrow(() -> new RuntimeException("El rol PRESIDENTE no está registrado."));

        // Crear el usuario presidente
        UsuarioEntity presidente = new UsuarioEntity();
        presidente.setNombre("GIANCARLO ANDRE CHAVEZ PEZO");
        presidente.setCorreo(correoPresidente);
        presidente.setClub(club);
        presidente.setContrasena(passwordEncoder.encode("1234567890"));
        presidente.setFechaNacimiento(LocalDate.of(1995, 5, 20));
        presidente.setActivo(true);
        presidente.setRol(rolPresidente);

        usuarioRepository.save(presidente);
        System.out.println("✅ Usuario PRESIDENTE creado con éxito: " + correoPresidente);
    }
}
