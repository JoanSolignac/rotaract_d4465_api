package com.unapi.rotaract.rotaract_d4465_api.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.AuthResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.ForgotPasswordRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.LoginRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.RegistroRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.ResetPasswordRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.jwt.JwtService;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;

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
    private final IEmailService emailService;

    // -------------------------------------------
    // LOGIN
    // -------------------------------------------
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

    // -------------------------------------------
    // REGISTER
    // -------------------------------------------
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

    // -------------------------------------------
    // 🔥 RECUPERAR CONTRASEÑA (FORGOT PASSWORD)
    // -------------------------------------------
    public void forgotPassword(ForgotPasswordRequestDto request) {

        UsuarioEntity usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow(() -> new UsernameNotFoundException("Correo no registrado"));

        String resetToken = jwtService.generatePasswordResetToken(usuario);

        String resetLink = "https://rotaractd4465.com/reset-password?token=" + resetToken;

        String html = """
                <h2>Recuperación de contraseña - Rotaract D4465</h2>
                <p>Hola, has solicitado recuperar tu contraseña.</p>
                <p>Haz clic en el siguiente enlace para continuar:</p>
                <a href="%s" style="color:#8C1D40;font-weight:bold;">Restablecer contraseña</a>
                <p>Este enlace caduca en 10 minutos.</p>
                """.formatted(resetLink);

        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Recuperación de contraseña - Rotaract D4465",
                html
        );
    }

    // -------------------------------------------
    // 🔥 RESTABLECER CONTRASEÑA (RESET PASSWORD)
    // -------------------------------------------
    public void resetPassword(ResetPasswordRequestDto request) {

        String token = request.token();

        if (!jwtService.isTokenValid(token)) {
            throw new IllegalStateException("Token inválido o expirado");
        }

        if (!jwtService.isPasswordResetToken(token)) {
            throw new IllegalStateException("Token no autorizado para cambio de contraseña");
        }

        String correo = jwtService.extractUsername(token);

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setContrasena(passwordEncoder.encode(request.nuevaContrasena()));

        usuarioRepository.save(usuario);
    }
}
