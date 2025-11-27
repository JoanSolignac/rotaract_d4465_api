package com.unapi.rotaract.rotaract_d4465_api.auth.controller;

import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.UsuarioPerfilResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.UsuarioPerfilUpdateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.interfaces.IUsuarioService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @GetMapping("/perfil")
    public UsuarioPerfilResponseDto obtenerPerfil() {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.obtenerPerfil(correo);
    }

    @PutMapping("/perfil")
    public UsuarioPerfilResponseDto actualizarPerfil(
            @RequestBody UsuarioPerfilUpdateRequestDto request
    ) {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.actualizarPerfil(correo, request);
    }
}
