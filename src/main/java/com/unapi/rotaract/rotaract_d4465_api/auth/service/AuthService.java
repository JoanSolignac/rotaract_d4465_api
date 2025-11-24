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

    /**
     * Inicia sesión de un usuario registrado.
     */
    public AuthResponseDto login(@Valid LoginRequestDto loginRequestDto) {

        // Autenticar credenciales
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

        // Obtener usuario
        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(loginRequestDto.correo())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Validar estado
        if (!usuarioEntity.getActivo()) {
            throw new IllegalStateException("El usuario se encuentra inactivo");
        }

        // Manejo seguro del clubId para evitar NullPointerException
        Long clubId = usuarioEntity.getClub() == null ? null : usuarioEntity.getClub().getId();

        // Generar tokens JWT
        String accessToken = jwtService.generateToken(usuarioEntity);
        String refreshToken = jwtService.generateRefreshToken(usuarioEntity);

        // Respuesta
        return new AuthResponseDto(
                accessToken,
                refreshToken,
                usuarioEntity.getCorreo(),
                usuarioEntity.getRol() != null ? usuarioEntity.getRol().getNombre() : null,
                usuarioEntity.getNombre(),
                clubId,
                usuarioEntity.getId()
        );
    }

    /**
     * Registra un nuevo usuario con rol INTERESADO.
     */
    public AuthResponseDto register(@Valid RegistroRequestDto registroRequestDto) {

        // Verificar correo duplicado
        if (usuarioRepository.findByCorreo(registroRequestDto.correo()).isPresent()) {
            throw new IllegalStateException("El correo ya se encuentra registrado");
        }

        // Rol INTERESADO
        RolEntity rolInteresado = rolRepository.findByNombre("INTERESADO")
                .orElseThrow(() -> new RuntimeException("Rol 'INTERESADO' no encontrado en el sistema"));

        // Crear nuevo usuario
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

        // Manejo seguro clubId
        Long clubId = nuevoUsuario.getClub() == null ? null : nuevoUsuario.getClub().getId();

        // Tokens
        String accessToken = jwtService.generateToken(nuevoUsuario);
        String refreshToken = jwtService.generateRefreshToken(nuevoUsuario);

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                nuevoUsuario.getCorreo(),
                nuevoUsuario.getRol().getNombre(),
                nuevoUsuario.getNombre(),
                clubId,
                nuevoUsuario.getId()
        );
    }
}
