package com.unapi.rotaract.rotaract_d4465_api.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.AuthResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.LoginRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.RegistroRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.jwt.JwtService;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDto login(@Valid LoginRequestDto loginRequestDto) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.correo(),
                            loginRequestDto.contrasena()
                    )
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(loginRequestDto.correo())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (!usuarioEntity.getActivo()) {
            throw new IllegalStateException("El usuario se encuentra inactivo");
        }

        String accessToken = jwtService.generateToken(usuarioEntity);
        String refreshToken = jwtService.generateRefreshToken(usuarioEntity);

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                usuarioEntity.getCorreo(),
                usuarioEntity.getRol().getNombre(),
                usuarioEntity.getNombre(),
                usuarioEntity.getId()
        );
    }

    public AuthResponseDto register(@Valid RegistroRequestDto registroRequestDto) {

        if (usuarioRepository.findByCorreo(registroRequestDto.correo()).isPresent()) {
            throw new IllegalStateException("El correo ya se encuentra registrado");
        }

        RolEntity rolInteresado = rolRepository.findByNombre("INTERESADO")
                .orElseThrow(() -> new RuntimeException("Rol 'INTERESADO' no encontrado en el sistema"));

        UsuarioEntity nuevoUsuario = UsuarioEntity.builder()
                .nombre(registroRequestDto.nombre().toUpperCase())
                .correo(registroRequestDto.correo().toLowerCase())
                .contrasena(passwordEncoder.encode(registroRequestDto.contrasena()))
                .ciudad(registroRequestDto.ciudad().toUpperCase())
                .fechaNacimiento(registroRequestDto.fechaNacimiento())
                .activo(true)
                .rol(rolInteresado)
                .build();

        usuarioRepository.save(nuevoUsuario);

        String accessToken = jwtService.generateToken(nuevoUsuario);
        String refreshToken = jwtService.generateRefreshToken(nuevoUsuario);

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                nuevoUsuario.getCorreo(),
                nuevoUsuario.getRol().getNombre(),
                nuevoUsuario.getNombre(),
                nuevoUsuario.getId()
        );
    }
}
