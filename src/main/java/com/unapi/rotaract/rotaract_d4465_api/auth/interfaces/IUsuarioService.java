package com.unapi.rotaract.rotaract_d4465_api.auth.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.UsuarioPerfilResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.UsuarioPerfilUpdateRequestDto;

public interface IUsuarioService {

    UsuarioPerfilResponseDto obtenerPerfil(String correo);

    UsuarioPerfilResponseDto actualizarPerfil(String correo, UsuarioPerfilUpdateRequestDto request);
}
