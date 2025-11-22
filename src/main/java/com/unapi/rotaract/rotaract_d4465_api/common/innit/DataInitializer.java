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

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClubRepository clubRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        inicializarRoles();
        inicializarClubes();
        inicializarUsuarios();
    }

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

    private void inicializarClubes(){
        Optional<ClubEntity> existente = clubRepository.findByNombre("ROTARACT IQUITOS AMAZONAS");
        if (existente.isEmpty()) {

            ClubEntity club = new ClubEntity();
            club.setNombre("ROTARACT IQUITOS AMAZONAS");
            club.setDepartamento("LORETO");
            club.setCiudad("IQUITOS");
            club.setFechaCreacion(LocalDate.now());
            club.setActivo(true);

            clubRepository.save(club);
            System.out.println("Club creado correctamente: ROTARACT IQUITOS AMAZONAS");
        } else {
            System.out.println("El club ya existe, no se creó nuevamente.");
        }
    }

    private void inicializarUsuarios(){

        // Rol Presidente
        RolEntity rolPresidente = rolRepository.findByNombre("PRESIDENTE")
                .orElseThrow(() -> new RuntimeException("Rol PRESIDENTE no encontrado"));

        // Buscar club
        ClubEntity club = clubRepository.findByNombre("ROTARACT IQUITOS AMAZONAS")
                .orElseThrow(() -> new RuntimeException("Club ROTARACT IQUITOS AMAZONAS no encontrado"));

        // Verificar si ya existe el presidente
        Optional<UsuarioEntity> existente = usuarioRepository.findByCorreo("joanpsolignac@gmail.com");

        if (existente.isEmpty()) {

            UsuarioEntity presidente = new UsuarioEntity();
            presidente.setNombre("JOAN PIERO SOLIGNAC LOVERA");
            presidente.setCorreo("joanpsolignac@gmail.com");
            presidente.setContrasena(passwordEncoder.encode("1234567890"));
            presidente.setFechaNacimiento(LocalDate.of(2003, 3, 8));
            presidente.setRol(rolPresidente);
            presidente.setActivo(true);
            presidente.setClub(club);

            usuarioRepository.save(presidente);

            System.out.println("✅ Presidente Joan Piero Solignac Lovera creado correctamente.");

        } else {
            System.out.println("ℹ️ El presidente Joan Piero Solignac Lovera ya existe, no se volvió a crear.");
        }
    }
}
