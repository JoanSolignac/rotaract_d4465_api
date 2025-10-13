package com.unapi.rotaract.rotaract_d4465_api.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de inicio de sesión de usuarios.
 * 
 * Permite autenticar un usuario existente en el sistema mediante su correo y contraseña.
 */
public record LoginRequestDto(

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo no debe exceder los 150 caracteres")
    String correo,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres")
    String contrasena

) {}
