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
import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Operaciones relacionadas con autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    /**
     * Inicia sesión de un usuario mediante correo y contraseña válidos.
     *
     * @param loginRequestDto DTO con las credenciales del usuario.
     * @return tokens de acceso y refresh en caso de éxito.
     */
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica a un usuario y devuelve tokens JWT de acceso y refresh si las credenciales son válidas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación correcta",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (datos no válidos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto)
    {
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param registroRequestDto DTO con los datos del nuevo usuario.
     * @return tokens de autenticación iniciales si el registro es exitoso.
     */
    @PostMapping("/register")
    @Operation(
        summary = "Registrar usuario",
        description = "Registra un nuevo usuario con el rol por defecto 'Interesado' y devuelve tokens JWT de autenticación."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registro exitoso",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "409", description = "El usuario ya existe",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegistroRequestDto registroRequestDto)
    {
        AuthResponseDto response = authService.register(registroRequestDto);
        return ResponseEntity.ok(response);
    }

}
