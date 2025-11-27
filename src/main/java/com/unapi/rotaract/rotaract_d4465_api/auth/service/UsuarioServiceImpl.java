package com.unapi.rotaract.rotaract_d4465_api.auth.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.UsuarioPerfilResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.UsuarioPerfilUpdateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.interfaces.IUsuarioService;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UsuarioPerfilResponseDto obtenerPerfil(String correo) {

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return mapToDto(usuario);
    }

    @Override
    @Transactional
    public UsuarioPerfilResponseDto actualizarPerfil(String correoActual, UsuarioPerfilUpdateRequestDto request) {

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correoActual)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar nombre
        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.setNombre(request.nombre());
        }

        // Actualizar correo
        if (request.correo() != null && !request.correo().equals(usuario.getCorreo())) {

            boolean correoOcupado = usuarioRepository.findByCorreo(request.correo()).isPresent();
            if (correoOcupado) {
                throw new RuntimeException("El correo ya está registrado");
            }

            usuario.setCorreo(request.correo());
        }

        // Actualizar ciudad
        if (request.ciudad() != null) {
            usuario.setCiudad(request.ciudad());
        }

        usuarioRepository.save(usuario);

        return mapToDto(usuario);
    }

    private UsuarioPerfilResponseDto mapToDto(UsuarioEntity u) {
        return new UsuarioPerfilResponseDto(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getCiudad(),
                u.getFechaNacimiento(),
                u.getRol().getNombre(),
                u.getClub() != null ? u.getClub().getId() : null
        );
    }
}
