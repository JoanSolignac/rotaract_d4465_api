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

    // Métodos para manejar las solicitudes de autenticación

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto)
    {
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegistroRequestDto registroRequestDto)
    {
        AuthResponseDto response = authService.register(registroRequestDto);
        return ResponseEntity.ok(response);
    }

}
