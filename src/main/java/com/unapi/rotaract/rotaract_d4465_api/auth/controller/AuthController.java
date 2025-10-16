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
     * Realiza la autenticación de un usuario mediante sus credenciales.
     *
     * Valida el cuerpo de la petición proporcionado en el objeto LoginRequestDto
     * y delega en el servicio de autenticación la generación de la respuesta.
     *
     * @param loginRequestDto DTO que contiene el correo y la contraseña del usuario; debe ser válido
     * @return ResponseEntity con un AuthResponseDto y estado HTTP 200 (OK)
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve tokens de acceso y refresh si las credenciales son válidas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación correcta", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida: datos de entrada no válidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
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
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario y devuelve tokens de autenticación iniciales.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registro correcto", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida: datos de entrada no válidos"),
        @ApiResponse(responseCode = "409", description = "Usuario ya existe")
    })
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegistroRequestDto registroRequestDto)
    {
        AuthResponseDto response = authService.register(registroRequestDto);
        return ResponseEntity.ok(response);
    }

}
