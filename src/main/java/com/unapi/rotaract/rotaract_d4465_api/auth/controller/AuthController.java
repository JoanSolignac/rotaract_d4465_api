package com.unapi.rotaract.rotaract_d4465_api.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unapi.rotaract.rotaract_d4465_api.auth.dtos.*;
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

/**
 * Controlador REST para operaciones de autenticación y gestión de credenciales.
 * Expone endpoints públicos bajo el prefijo {@code /auth}.
 *
 * Endpoints incluidos:
 * - POST /auth/login
 * - POST /auth/register
 * - POST /auth/forgot-password
 * - POST /auth/reset-password
 *
 * Los errores se devuelven mediante {@link ExceptionResponseDto}.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Operaciones relacionadas con autenticación, registro y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;

    // ================================================================
    //                         LOGIN
    // ================================================================
    /**
     * Inicia sesión de un usuario mediante correo y contraseña.
     *
     * @param loginRequestDto DTO con las credenciales del usuario.
     * @return tokens de acceso y refresh.
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
            @ApiResponse(responseCode = "400", description = "Datos no válidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<AuthResponseDto> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales de acceso",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
            )
            @Valid @RequestBody LoginRequestDto loginRequestDto)
    {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    // ================================================================
    //                         REGISTER
    // ================================================================
    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param registroRequestDto datos del nuevo usuario.
     * @return tokens JWT iniciales.
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuario",
            description = "Registra un nuevo usuario con el rol por defecto 'INTERESADO' y devuelve tokens JWT de autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro exitoso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos no válidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<AuthResponseDto> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para registrar un nuevo usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegistroRequestDto.class))
            )
            @Valid @RequestBody RegistroRequestDto registroRequestDto)
    {
        return ResponseEntity.ok(authService.register(registroRequestDto));
    }

    // ================================================================
    //                     FORGOT PASSWORD
    // ================================================================
    /**
     * Envia un correo con un enlace temporal (10 minutos) para restablecer la contraseña.
     *
     * @param request DTO con el correo del usuario.
     * @return mensaje de confirmación.
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Envía un enlace temporal al correo del usuario para restablecer la contraseña. No requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correo enviado correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Correo no registrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<String> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Correo del usuario que desea recuperar su contraseña",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ForgotPasswordRequestDto.class))
            )
            @Valid @RequestBody ForgotPasswordRequestDto request)
    {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Correo de recuperación enviado correctamente");
    }

    // ================================================================
    //                       RESET PASSWORD
    // ================================================================
    /**
     * Restablece la contraseña mediante un token temporal enviado al correo.
     *
     * @param request DTO con el token y la nueva contraseña.
     * @return mensaje de éxito.
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer contraseña",
            description = "Permite establecer una nueva contraseña utilizando un token temporal (válido por 10 minutos)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    public ResponseEntity<String> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Token y nueva contraseña",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResetPasswordRequestDto.class))
            )
            @Valid @RequestBody ResetPasswordRequestDto request)
    {
        authService.resetPassword(request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
