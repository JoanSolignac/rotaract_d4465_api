package com.unapi.rotaract.rotaract_d4465_api.common.innit;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

@RequiredArgsConstructor
public class DatatInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) throws Exception {

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
                            System.out.println("✅ Rol creado: " + guardado.getNombre());
                            return guardado;
                        })
        );

        System.out.println("✔ Inicialización de roles completada correctamente.");
    }
}
