package com.unapi.rotaract.rotaract_d4465_api.common.exception;

import com.unapi.rotaract.rotaract_d4465_api.common.dtos.ExceptionResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para la aplicación Rotaract D4465.
 *
 * Esta clase intercepta y transforma las excepciones lanzadas por los controladores
 * en respuestas JSON estandarizadas, de modo que el cliente reciba mensajes claros,
 * códigos HTTP coherentes y detalles útiles para depuración.
 *
 * Los errores más frecuentes (validación, autenticación, duplicados, etc.)
 * tienen su propio manejador. Solo los errores no previstos caen en el bloque 500.
 */
@RestControllerAdvice
@Hidden
public class GlobalHandlerException {

    private static final Logger logger = LoggerFactory.getLogger(GlobalHandlerException.class);

    // -------------------------------------------------------------------------
    // 401 - UNAUTHORIZED
    // -------------------------------------------------------------------------
    /**
     * Captura errores de autenticación (token inválido, credenciales incorrectas, etc.).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        logger.warn("Error de autenticación: {}", ex.getMessage());
        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.UNAUTHORIZED.value(),
                List.of(ex.getMessage()),
                "Error de autenticación"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // -------------------------------------------------------------------------
    // 403 - FORBIDDEN
    // -------------------------------------------------------------------------
    /**
     * Captura intentos de acceso no autorizado por falta de permisos o roles.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponseDto> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Acceso denegado: {}", ex.getMessage());
        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.FORBIDDEN.value(),
                List.of("No tienes permisos suficientes para realizar esta acción."),
                "Acceso denegado"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // -------------------------------------------------------------------------
    // 404 - NOT FOUND
    // -------------------------------------------------------------------------
    /**
     * Captura excepciones cuando una entidad solicitada no existe en la base de datos.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        logger.info("Recurso no encontrado: {}", ex.getMessage());
        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.NOT_FOUND.value(),
                List.of("El recurso solicitado no fue encontrado."),
                "No encontrado"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // -------------------------------------------------------------------------
    // 409 - CONFLICT (Restricciones de base de datos - Hibernate)
    // -------------------------------------------------------------------------
    /**
     * Captura violaciones de restricciones de integridad a nivel de Hibernate,
     * como duplicados en columnas con restricción UNIQUE (ejemplo: email).
     * Se lanza antes de que la excepción sea envuelta en DataIntegrityViolationException.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponseDto> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        logger.warn("Violación de restricción: {}", ex.getConstraintName());

        String mensajeError = "La operación viola una restricción de datos.";
        if (ex.getConstraintName() != null) {
            String constraint = ex.getConstraintName().toLowerCase();
            if (constraint.contains("email")) {
                mensajeError = "El correo electrónico ya se encuentra registrado.";
            } else if (constraint.contains("usuario") || constraint.contains("username")) {
                mensajeError = "El nombre de usuario ya está en uso.";
            } else if (constraint.contains("dni")) {
                mensajeError = "El número de documento ya se encuentra registrado.";
            }
        }

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.CONFLICT.value(),
                List.of(mensajeError),
                "Conflicto de datos"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // -------------------------------------------------------------------------
    // 409 - CONFLICT (Spring DataIntegrityViolation)
    // -------------------------------------------------------------------------
    /**
     * Captura errores de integridad lanzados por Spring (ejemplo: valores duplicados).
     * Este método es un respaldo en caso de que Hibernate no dispare su excepción interna.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.warn("Conflicto de integridad de datos: {}", ex.getMessage());

        String mensajeError = "La operación viola una restricción de datos (valor duplicado).";
        if (ex.getMessage() != null) {
            String msg = ex.getMessage().toLowerCase();
            if (msg.contains("email")) {
                mensajeError = "El correo electrónico ya se encuentra registrado.";
            } else if (msg.contains("username") || msg.contains("usuario")) {
                mensajeError = "El nombre de usuario ya está en uso.";
            }
        }

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.CONFLICT.value(),
                List.of(mensajeError),
                "Conflicto de datos"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // -------------------------------------------------------------------------
    // 400 - BAD REQUEST
    // -------------------------------------------------------------------------
    /**
     * Captura errores de validación de DTOs con anotaciones @Valid.
     * Devuelve el detalle de los campos con errores.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        logger.info("Error de validación de la solicitud: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ExceptionResponseDto(
                        LocalDate.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        errors,
                        "Solicitud incorrecta"
                )
        );
    }

    // -------------------------------------------------------------------------
    // 500 - INTERNAL SERVER ERROR (Fallback)
    // -------------------------------------------------------------------------
    /**
     * Manejador de último recurso. Captura cualquier excepción no controlada
     * por los bloques anteriores (NullPointerException, errores de lógica, etc.).
     * Loguea el stack trace completo para diagnóstico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleGeneralException(Exception ex, HttpServletRequest request) {
        logger.error("Error interno del servidor no controlado:", ex);

        ExceptionResponseDto body = new ExceptionResponseDto(
                LocalDate.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                List.of("Ha ocurrido un error interno inesperado. Contacte al administrador."),
                "Error interno del servidor"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
