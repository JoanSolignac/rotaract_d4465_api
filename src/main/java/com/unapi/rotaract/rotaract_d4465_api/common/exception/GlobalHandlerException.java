package com.unapi.rotaract.rotaract_d4465_api.common.exception;

import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException; // Para 404 de JPA
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

// Logging: ¡Importante para ver los errores 500!
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException; // Para 409
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Para 403
import org.springframework.security.core.AuthenticationException; // Para 401
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden
public class GlobalHandlerException {

    // 1. AÑADIMOS UN LOGGER
    // Esto es crucial para que puedas ver el stack trace de los errores 500 en tu consola.
    private static final Logger logger = LoggerFactory.getLogger(GlobalHandlerException.class);

    /**
     * MANEJADOR 401 (UNAUTHORIZED)
     * Captura errores de autenticación (ej. BadCredentialsException).
     * Se dispara cuando las credenciales (token, usuario/pass) son incorrectas.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        logger.warn("Error de autenticación: {}", ex.getMessage()); // Logeamos como advertencia

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.UNAUTHORIZED.value(), // 401
                List.of(ex.getMessage()),
                "Error de autenticación"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * MANEJADOR 403 (FORBIDDEN)
     * Captura errores de autorización (ej. falta de roles).
     * Se dispara cuando un usuario autenticado intenta hacer algo para lo que no tiene permisos.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponseDto> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Acceso denegado: {}", ex.getMessage());

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.FORBIDDEN.value(), // 403
                List.of("No tienes permisos suficientes para realizar esta acción."),
                "Acceso denegado"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * MANEJADOR 404 (NOT FOUND)
     * Captura errores cuando no se encuentra una entidad en la BD (JPA).
     * Se dispara con métodos como `repository.getReferenceById()` si no lo encuentra.
     * (Nota: `findById` devuelve Optional, por lo que deberías lanzar esta excepción tú mismo).
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        logger.info("Recurso no encontrado: {}", ex.getMessage());

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.NOT_FOUND.value(), // 404
                List.of("El recurso solicitado no fue encontrado."),
                "No encontrado"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * MANEJADOR 409 (CONFLICT)
     * Captura violaciones de integridad de la base de datos (ej. duplicados).
     * Se dispara si intentas insertar un registro que viola una restricción 'UNIQUE' (ej. email ya registrado).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.warn("Conflicto de integridad de datos: {}", ex.getMessage());

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.CONFLICT.value(), // 409
                List.of("La operación viola una restricción de datos (ej. un valor ya existe)."),
                "Conflicto de datos"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * MANEJADOR 400 (BAD REQUEST)
     * (Este ya lo tenías y estaba perfecto).
     * Captura errores de validación de DTOs anotados con @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        logger.info("Error de validación de la solicitud: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( // 400
                new ExceptionResponseDto(
                        LocalDate.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        errors,
                        "Solicitud incorrecta"
                )
        );
    }

    /**
     * MANEJADOR 500 (INTERNAL SERVER ERROR) - EL ÚLTIMO RECURSO
     * (Tu manejador original, ahora modificado).
     * Captura CUALQUIER OTRA excepción no manejada (ej. NullPointerException).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleGeneralException(Exception ex, HttpServletRequest request) {

        // ¡¡ESTA ES LA LÍNEA MÁS IMPORTANTE!!
        // Logeamos el error completo (con stack trace) en la consola del servidor.
        logger.error("Error interno del servidor no controlado:", ex);

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                List.of("Ha ocurrido un error interno inesperado. Contacte al administrador."),
                "Error interno del servidor"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}