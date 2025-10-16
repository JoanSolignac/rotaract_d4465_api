package com.unapi.rotaract.rotaract_d4465_api.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.AuthResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.LoginRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.RegistroRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Realiza la autenticación de un usuario mediante sus credenciales.
     *
     * Valida el cuerpo de la petición proporcionado en el objeto LoginRequestDto
     * y delega en el servicio de autenticación la generación de la respuesta.
     *
     * @param loginRequestDto DTO que contiene el correo y la contraseña del usuario; debe ser válido
     * @return ResponseEntity con un AuthResponseDto y estado HTTP 200 (OK)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto)
    {
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * El controlador recibe un RegistroRequestDto validado y delega en el
     * servicio de autenticación la creación del usuario. A continuación
     * retorna un AuthResponseDto que contiene la información de autenticación
     * inicial, incluidos los tokens.
     *
     * @param registroRequestDto DTO que contiene los datos requeridos para el registro; debe ser válido
     * @return ResponseEntity con un AuthResponseDto y estado HTTP 200 (OK)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegistroRequestDto registroRequestDto)
    {
        AuthResponseDto response = authService.register(registroRequestDto);
        return ResponseEntity.ok(response);
    }

}
