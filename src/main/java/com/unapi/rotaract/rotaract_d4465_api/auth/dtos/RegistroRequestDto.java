package com.unapi.rotaract.rotaract_d4465_api.auth.dtos;

import jakarta.validation.constraints.*;
import java.sql.Date;

/**
 * DTO para el registro de nuevos usuarios en la plataforma Rotaract D4465.
 * 
 * El rol por defecto asignado al registrarse será "INTERESADO".
 * Incluye validaciones de formato y obligatoriedad.
 */
public record RegistroRequestDto(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 150, message = "El correo no debe exceder los 150 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres")
        String contrasena,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(min = 2, max = 80, message = "La ciudad debe tener entre 2 y 80 caracteres")
        String ciudad,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        Date fechaNacimiento

) {}
